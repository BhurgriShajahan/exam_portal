package exam.portal.global_exceptions_handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomResponseEntity<T> {

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int errorCode;

    private boolean success;

    private String message;
    private T data;

    public CustomResponseEntity(T data, String message) {
        this.success = true;
        this.message = message;
        this.data = data;
    }

    public CustomResponseEntity(String message) {
        this.success = true;
        this.message = message;
    }

    public CustomResponseEntity(int errorCode, String message) {
        this.errorCode = errorCode;
        this.success = false;
        this.message = message;
    }

    public CustomResponseEntity(int errorCode, String message, T data) {
        this.errorCode = errorCode;
        this.success = false;
        this.message = message;
        this.data = data;
    }

    public static <T> CustomResponseEntity<T> errorResponse(Exception exception) {
        return new CustomResponseEntity<>(1, "An error occurred: " + exception.getMessage());
    }

    public static <T> CustomResponseEntity<T> error(String error) {
        return new CustomResponseEntity<>(1000, false ,error, null);
    }
}
