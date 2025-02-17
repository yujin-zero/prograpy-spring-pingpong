package prography.spring.pingpong.domain.user.model.dto;

import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
public class UserListResponseDto {
    private long totalElements;
    private int totalPages;
    private List<UserResponseDto> userList;

    public static UserListResponseDto fromPage(Page<UserResponseDto> userPage) {
        return UserListResponseDto.builder()
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .userList(userPage.getContent())
                .build();
    }
}
