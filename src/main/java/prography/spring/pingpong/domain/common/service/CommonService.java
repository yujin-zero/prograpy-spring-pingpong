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
        deleteAllData();

        List<FakerUserDto.FakerUser> fakerUsers = fetchFakerUsers(requestDto);
        if (fakerUsers == null) {
            return ApiResponse.serverError();
        }

        List<User> users = convertToUserEntities(fakerUsers);
        userRepository.saveAll(users);
        log.info("✅ [Common] User 데이터 저장 완료");

        return ApiResponse.success(null);
    }

    private void deleteAllData() {
        log.info("📌 [CommonService] 모든 데이터 삭제 중...");
        userRoomRepository.deleteAll();
        userRepository.deleteAll();
        roomRepository.deleteAll();
    }

    private List<FakerUserDto.FakerUser> fetchFakerUsers(InitRequestDto requestDto) {
        log.info("📌 [CommonService] Faker API 호출 (seed={}, quantity={})", requestDto.seed(), requestDto.quantity());

        String apiUrl = String.format("https://fakerapi.it/api/v1/users?_seed=%d&_quantity=%d&_locale=ko_KR",
                requestDto.seed(), requestDto.quantity());

        FakerUserDto response = restTemplate.getForObject(apiUrl, FakerUserDto.class);

        if (response == null || response.getData() == null) {
            log.error("🚨 [CommonService] Faker API 응답 없음");
            return null;
        }

        log.info("✅ [CommonService] Faker API 응답 성공 (총 {}명)", response.getData().size());
        return response.getData();
    }

    private List<User> convertToUserEntities(List<FakerUserDto.FakerUser> fakerUsers) {
        log.info("📌 [CommonService] User 엔티티 변환 중...");

        List<User> users = fakerUsers.stream()
                .sorted(Comparator.comparingInt(FakerUserDto.FakerUser::getId)) // fakerId 기준 정렬
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
                }).toList();

        log.info("✅ [CommonService] User 엔티티 변환 완료 (총 {}명)", users.size());
        return users;
    }
}
