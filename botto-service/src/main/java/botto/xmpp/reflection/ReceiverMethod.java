package botto.xmpp.reflection;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import botto.xmpp.annotations.Receive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.Packet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ReceiverMethod {

    private static final Logger Log = LoggerFactory.getLogger(ReceiverMethod.class);

    private final Method method;
    private final Class<?>[] arguments;
    private final int receiverArgument;

    private ReceiverMethod(Method method, Class<?>[] arguments, int receiverArgument) {
        this.method = method;
        this.arguments = arguments;
        this.receiverArgument = receiverArgument;
    }

    public boolean canReceive(Packet packet) {
        return arguments[receiverArgument].isAssignableFrom(packet.getClass());
    }

    public Optional<Packet> receive(Object instance,Packet packet) {
        Object[] args = new Object[arguments.length];
        args[receiverArgument] = packet;
        try {
            Object response = method.invoke(instance, args);
            if (response == null || ! Packet.class.isAssignableFrom(response.getClass())) {
                return Optional.absent();
            }
            return Optional.of((Packet)response);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<ReceiverMethod> from(Method method) {
        Receive annotation = method.getAnnotation(Receive.class);
        if (annotation == null) {
            return Optional.absent();
        }

        if (!Modifier.isPublic(method.getModifiers())) {
            Log.debug("Method marked as @Receiver is not valid because it is not public: {}", method);
            return Optional.absent();
        }

        if (method.getReturnType() != void.class && !Packet.class.isAssignableFrom(method.getReturnType())) {
            Log.debug("Method marked as @Receiver is not valid because it does not return void, Packet or a subclass of Packet: {}", method);
            return Optional.absent();
        }

        Class<?>[] arguments = method.getParameterTypes();

        // must have one and only one parameter of any subclass of Packet
        int packetParams = 0;
        int packetArgument = -1;

        for(int pos=0; pos < arguments.length; pos++) {
            if (Packet.class.isAssignableFrom(arguments[pos])) {
                packetArgument = pos;
                packetParams++;
            }
        }

        if (packetParams != 1) {
            Log.debug("Method marked as @Receiver is not valid because it has more than one parameter ({}) that accepts Packet or a subclass of Packet: {}", packetParams, method);
            return Optional.absent();
        }

        return Optional.of(new ReceiverMethod(method, arguments, packetArgument));
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .addValue(method)
            .toString();
    }
}
