package botto.xmpp.botto.xmpp.connector;


import com.google.common.base.Objects;

// TODO: in an ideal world, only the ConnectionManager should be able to create ConnectorId
public final class ConnectorId {
    private final String toString;
    private final int hashCode;

    private final int id;
    private final Class clazz;
    private final String name;

    public ConnectorId(int id, Class clazz, String name) {
        this.id = id;
        this.clazz = clazz;
        this.name = name;
        this.toString = Objects.toStringHelper(this)
            .addValue("#" + id)
            .addValue(name)
            .addValue(clazz.getSimpleName())
            .toString();

        this.hashCode = Objects.hashCode(id, clazz, name);
    }

    public int getId() {
        return id;
    }

    public Class getClazz() {
        return clazz;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return toString;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;

        if (getClass() != obj.getClass())
            return false;

        final ConnectorId other = (ConnectorId) obj;
        return Objects.equal(id, other.id)
            && Objects.equal(clazz, other.clazz)
            && Objects.equal(name, other.name);
    }
}
