package thomas.com.EventPing.event.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import thomas.com.EventPing.event.model.EventCustomField;
import thomas.com.EventPing.security.validation.NoSqlInjection;
import thomas.com.EventPing.security.validation.NoXss;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomFieldDto {
    
    @NotBlank(message = "Field name is required")
    @Size(max = 255, message = "Field name must not exceed 255 characters")
    @NoXss
    @NoSqlInjection
    private String fieldName;

    @NotNull(message = "Field type is required")
    private EventCustomField.FieldType fieldType;

    private boolean required = false;

    @Size(max = 255, message = "Placeholder text must not exceed 255 characters")
    @NoXss
    @NoSqlInjection
    private String placeholderText;

    @NoXss
    @NoSqlInjection
    private String fieldOptions; // Comma-separated for SELECT type

    private int displayOrder = 0;
}
