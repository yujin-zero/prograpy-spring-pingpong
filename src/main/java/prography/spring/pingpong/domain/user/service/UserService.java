package prography.spring.pingpong.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prography.spring.pingpong.domain.room.model.entity.Room;
import prography.spring.pingpong.domain.room.repository.RoomRepository;
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
    private final RoomRepository roomRepository;

    @Transactional(readOnly = true)
    public ApiResponse<UserListResponseDto> getAllUsers(int page, int size) {
        log.info("📌 [User] 유저 전체 조회 요청 (page={}, size={})",page,size);

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Direction.ASC, "id"));
        Page<UserResponseDto> userPage = userRepository.findAll(pageRequest)
                .map(UserResponseDto::fromEntity);

        return ApiResponse.success(UserListResponseDto.fromPage(userPage));
    }

    public boolean isValidHost(Long roomId, Long userId) {
        Room room = roomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return false;
        }
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        return room.getHost().equals(user);
    }
}

