package az.gdg.msauth.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoForBlogService {

    private String name;
    private String surname;
    private String imgUrl;

}
