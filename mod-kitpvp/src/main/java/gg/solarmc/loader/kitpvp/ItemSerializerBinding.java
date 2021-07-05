package gg.solarmc.loader.kitpvp;

import org.jooq.Binding;
import org.jooq.BindingGetResultSetContext;
import org.jooq.BindingGetSQLInputContext;
import org.jooq.BindingGetStatementContext;
import org.jooq.BindingRegisterContext;
import org.jooq.BindingSQLContext;
import org.jooq.BindingSetSQLOutputContext;
import org.jooq.BindingSetStatementContext;
import org.jooq.Converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

record ItemSerializerBinding(ItemSerializer itemSerializer) implements Binding<byte[], KitItem> {

    private SQLException rethrow(IOException ex) {
        return new SQLException("Unable to write data to stream", ex);
    }

    @Override
    public void set(BindingSetStatementContext<KitItem> ctx) throws SQLException {
        PreparedStatement statement = ctx.statement();
        Blob blob = statement.getConnection().createBlob();

        KitItem kitItem = Objects.requireNonNull(ctx.value(), "kit item");
        try (OutputStream output = blob.setBinaryStream(1)) {
            itemSerializer.serialize(kitItem, output);
        } catch (IOException ex) {
            throw rethrow(ex);
        }
        statement.setBlob(ctx.index(), blob);
        ctx.autoFree(blob);
    }

    @Override
    public void get(BindingGetResultSetContext<KitItem> ctx) throws SQLException {
        ResultSet resultSet = ctx.resultSet();
        Blob blob = resultSet.getBlob(ctx.index());

        KitItem kitItem;
        try (InputStream input = blob.getBinaryStream()) {
            kitItem = itemSerializer.deserialize(input);
        } catch (IOException ex) {
            throw rethrow(ex);
        }
        ctx.value(kitItem);
        blob.free();
    }

    @Override
    public Converter<byte[], KitItem> converter() {
        return new Converter<>() {

            @Override
            public KitItem from(byte[] databaseObject) {
                if (databaseObject == null) {
                    return null;
                }
                throw new UnsupportedOperationException("Conversion should be streamed instead");
            }

            @Override
            public byte[] to(KitItem userObject) {
                throw new UnsupportedOperationException("Conversion should be streamed instead");
            }

            @Override
            public Class<byte[]> fromType() {
                return byte[].class;
            }

            @Override
            public Class<KitItem> toType() {
                return KitItem.class;
            }

        };
    }

    @Override
    public void sql(BindingSQLContext<KitItem> ctx) throws SQLException {
        String sql = switch (ctx.render().paramType()) {
            case INLINED -> "b'0'"; // temporary
            case INDEXED, FORCE_INDEXED, NAMED, NAMED_OR_INLINED -> ctx.variable();
        };
        ctx.render().sql(sql);
    }

    private UnsupportedOperationException notImplemented() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void register(BindingRegisterContext<KitItem> ctx) throws SQLException {
        throw notImplemented();
    }

    @Override
    public void set(BindingSetSQLOutputContext<KitItem> ctx) throws SQLException {
        throw notImplemented();
    }

    @Override
    public void get(BindingGetStatementContext<KitItem> ctx) throws SQLException {
        throw notImplemented();
    }

    @Override
    public void get(BindingGetSQLInputContext<KitItem> ctx) throws SQLException {
        throw notImplemented();
    }
}
