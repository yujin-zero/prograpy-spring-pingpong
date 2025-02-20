package prography.spring.pingpong.domain.user.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prography.spring.pingpong.domain.user.model.dto.UserListResponseDto;
import prography.spring.pingpong.domain.user.model.dto.UserResponseDto;
import prography.spring.pingpong.domain.user.model.entity.User;
import prography.spring.pingpong.domain.user.repository.UserRepository;
import prography.spring.pingpong.model.dto.ApiResponse;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ApiResponse<UserListResponseDto> getAllUsers(int page, int size) {
        log.info("📌 [User] 유저 전체 조회 요청 (page={}, size={})",page,size);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Direction.ASC, "id"));
        Page<UserResponseDto> userPage = userRepository.findAll(pageRequest)
                .map(UserResponseDto::fromEntity);

        return ApiResponse.success(UserListResponseDto.fromPage(userPage));
    }

    @Transactional
    public void deleteAllUsers() {
        userRepository.deleteAll();
        log.info("✅ [UserService] 모든 사용자 삭제 완료");
    }

    @Transactional
    public void saveAllUsers(List<User> users) {
        userRepository.saveAll(users);
        log.info("✅ [UserService] {}명의 사용자 저장 완료", users.size());
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }
}

