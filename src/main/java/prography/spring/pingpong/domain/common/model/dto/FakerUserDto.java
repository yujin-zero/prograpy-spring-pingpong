package prography.spring.pingpong.domain.common.model.dto;

import java.util.List;
import lombok.Getter;

@Getter
public class FakerUserDto {
    private List<FakerUser> data;

    @Getter
    public static class FakerUser {
        private int id;
        private String username;
        private String email;
    }
}
