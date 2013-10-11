package botto.xmpp.reflection;

import botto.xmpp.annotations.BotContext;
import botto.xmpp.AbstractBot;
import botto.xmpp.utils.Packets;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import botto.xmpp.annotations.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Packet;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

public class AnnotatedBotObject extends AbstractBot {

    private final Logger Log = LoggerFactory.getLogger(AnnotatedBotObject.class);

    private final Class<?> clazz;
    private final Object obj;
    private final Collection<Field> injectableFields;
    private final Collection<ReceiverMethod> receiverMethods;

    private AnnotatedBotObject(Object obj) {
        Preconditions.checkNotNull(obj, "Bot object must not be null.");
        this.obj = obj;
        this.clazz = obj.getClass();
        injectableFields = scanInjectableFields();
        receiverMethods = scanReceiverMethods();
    }

    public static Optional<AnnotatedBotObject> from(Object obj) {
        Preconditions.checkNotNull(obj, "Bot object must not be null.");
        AnnotatedBotObject annotated = new AnnotatedBotObject(obj);
        if (annotated.injectableFields.size() > 0 || annotated.receiverMethods.size() > 0) {
            return Optional.of(annotated);
        }
        return Optional.absent();
    }

    private Collection<Field> scanInjectableFields() {
        ArrayList<Field> injectables = new ArrayList<Field>();
        for(Field field : ReflectionUtils.fields(clazz)) {
            if (isInjectable(field)) {
                Log.debug("Found injectable field: {}", field);
                injectables.add(field);
            }
        }
        Log.debug("Found {} injectable fields", injectables);
        return injectables;
    }

    private Collection<ReceiverMethod> scanReceiverMethods() {
        ArrayList<ReceiverMethod> receivers = new ArrayList<ReceiverMethod>();
        for(Method method : ReflectionUtils.methods(clazz)) {
            Optional<ReceiverMethod> receiver = ReceiverMethod.from(method);
            if (receiver.isPresent()) {
                Log.debug("Found receiver method: {}", method);
                receivers.add(receiver.get());
            }
        }
        Log.debug("Found {} receiver methods", receivers.size());
        return receivers;
    }

    private boolean isInjectable(Field field) {
        Context annotation = field.getAnnotation(Context.class);
        if (annotation == null) {
            return false;
        }
        else if (BotContext.class.isAssignableFrom(field.getType())) {
            return true;
        }
        else {
            Log.debug("Field marked @Context is not valid because its type is not supported : {}", field);
            return false;
        }
    }

    // delivers an incoming packet to the first
    // method annotated @Receive that can receive
    // the specific type of this packet
    protected Packet doReceive(Packet packet) {
        for (ReceiverMethod method : receiverMethods) {
            if (method.canReceive(packet)) {
                Log.debug("Delivering {} to method {}", Packets.toString(packet), method);
                return method.receive(obj, packet).orNull();
            }
        }
        return null;
    }

    private void inject(Object value) {
        for(Field field : injectableFields) {
            if (!Modifier.isPublic(field.getModifiers())) {
                field.setAccessible(true);
            }

            Log.debug("Injecting {} to field {}", value, field);

            if (!field.getType().isAssignableFrom(value.getClass())) {
                Log.debug("Injected value {} is not compatible with field {}", value.getClass(), field.getType());
                continue;
            }

            try {
                field.set(obj, value);
                Log.debug("Injected {} to field {}", value.getClass(), field);
                return;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    protected void doSetcontext(BotContext botContext) {
        Preconditions.checkNotNull(botContext, "botContext can't be null");
        inject(botContext);
    }

    public Object getObject() {
        return obj;
    }

    public void shutdown() {
        // TODO: shutdown bot (set disconnected and refuse any other action)
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("bot", obj)
            .toString();
    }
}
