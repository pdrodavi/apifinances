package br.com.pedrodavi.financesapi.model.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAuthDTO {

    private String email;
    private String senha;

}
