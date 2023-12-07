package hexlet.code.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * urls table.
 */
@ToString
@Getter

public final class Url {

    @Setter
    private Long id;
    @ToString.Include
    private String name;
    private Timestamp createdAt;
    private Timestamp lastCheck;

    public Url(String name, Timestamp createdAt) {
        this.name = name;
        this.createdAt = createdAt;
    }

    public Url(String name, Timestamp createdAt, Timestamp lastCheck) {
        this.name = name;
        this.createdAt = createdAt;
        this.lastCheck = lastCheck;
    }

    /**
     * @param timestamp
     * @return date in string format.
     */
    public static @NotNull String convertTimestampToDate(Timestamp timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        return dateFormat.format(timestamp);
    }

    /**
     * @return date in string format.
     */
    public @NotNull String getStringCreatedAt() {
        return convertTimestampToDate(createdAt);
    }

    /**
     * @return date in string format.
     */
    public @NotNull String getStringLastCheck() {
        return lastCheck == null ? "" : convertTimestampToDate(lastCheck);
    }
}
