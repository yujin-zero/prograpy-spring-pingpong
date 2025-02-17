package prography.spring.pingpong.domain.common.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import prography.spring.pingpong.domain.common.model.dto.FakerUserDto;
import prography.spring.pingpong.domain.common.model.dto.FakerUserDto.FakerUser;
import prography.spring.pingpong.domain.common.model.dto.InitRequestDto;
import prography.spring.pingpong.domain.room.repository.RoomRepository;
import prography.spring.pingpong.domain.user.model.entity.User;
import prography.spring.pingpong.domain.user.repository.UserRepository;
import prography.spring.pingpong.domain.userroom.repository.UserRoomRepository;
import prography.spring.pingpong.model.dto.ApiResponse;
import prography.spring.pingpong.model.entity.UserStatus;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommonService {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final UserRoomRepository userRoomRepository;
    private final RestTemplate restTemplate;

    @Transactional
    public ApiResponse<Void> initializeData(InitRequestDto requestDto) {
        // 기존 데이터 삭제
        log.info("📌 [commonService] 모든 데이터 삭제 중");
        userRoomRepository.deleteAll();
        userRepository.deleteAll();
        roomRepository.deleteAll();

        // Faker API 호출
        log.info("📌 [commonService] Faker API 호출");
        String apiUrl = String.format("https://fakerapi.it/api/v1/users?_seed=%d&_quantity=%d&_locale=ko_KR",
                requestDto.seed(), requestDto.quantity());
        FakerUserDto response = restTemplate.getForObject(apiUrl, FakerUserDto.class);

        if (response == null || response.getData() == null) {
            return ApiResponse.serverError();
        }

        List<FakerUserDto.FakerUser> fakerUsers = response.getData();
        List<User> users = fakerUsers.stream()
                .sorted(Comparator.comparingInt(FakerUser::getId))
                .map(fakerUser -> {
                    UserStatus status;
                    if (fakerUser.getId() <= 30) {
                        status = UserStatus.ACTIVE;
                    } else if (fakerUser.getId() <= 60) {
                        status = UserStatus.WAIT;
                    } else {
                        status = UserStatus.NON_ACTIVE;
                    }
                    return User.builder()
                            .fakerId(fakerUser.getId())
                            .name(fakerUser.getUsername())
                            .email(fakerUser.getEmail())
                            .userstatus(status)
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    }
                ).toList();

        userRepository.saveAll(users);
        log.info("✅ [Common] User 데이터 저장 완료");

        return ApiResponse.success(null);
    }
}
