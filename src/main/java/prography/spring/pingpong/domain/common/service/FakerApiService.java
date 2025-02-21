package prography.spring.pingpong.domain.common.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import prography.spring.pingpong.domain.common.model.dto.FakerUserDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class FakerApiService {

    private final RestTemplate restTemplate;

    @Value("${faker.api-url}")
    private  String FAKE_API_URL;

    public List<FakerUserDto.FakerUser> fetchFakerUsers(int seed, int quantity) {
        String apiUrl = String.format(FAKE_API_URL, seed, quantity);
        log.info("📌 [FakerApiService] Faker API 호출 (URL: {})",apiUrl);

        FakerUserDto response = restTemplate.getForObject(apiUrl, FakerUserDto.class);
        if (response == null || response.getData() == null) {
            log.error("🚨 [FakerApiService] Faker API 응답 없음");
            return List.of();
        }

        log.info("✅ [FakerApiService] Faker API 응답 성공 (총 {}명)", response.getData().size());
        return response.getData();
    }
}
