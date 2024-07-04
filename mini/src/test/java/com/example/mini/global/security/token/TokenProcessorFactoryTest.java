package com.example.mini.global.security.token;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TokenProcessorFactory 테스트")
class TokenProcessorFactoryTest {

    private TokenProcessorFactory tokenProcessorFactory;

    @Mock
    private GoogleTokenProcessor googleTokenProcessor;

    @Mock
    private KakaoTokenProcessor kakaoTokenProcessor;

    @Mock
    private JwtTokenProcessor jwtTokenProcessor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        List<TokenProcessor> processorList = Arrays.asList(googleTokenProcessor, kakaoTokenProcessor, jwtTokenProcessor);
        tokenProcessorFactory = new TokenProcessorFactory(processorList);
    }

    @Test
    @DisplayName("Google 토큰 프로세서 조회 성공")
    void testGetProcessorForGoogle() {
        TokenProcessor processor = tokenProcessorFactory.getProcessor("Google");
        assertNotNull(processor);
        assertTrue(processor instanceof GoogleTokenProcessor);
    }

    @Test
    @DisplayName("Kakao 토큰 프로세서 조회 성공")
    void testGetProcessorForKakao() {
        TokenProcessor processor = tokenProcessorFactory.getProcessor("Kakao");
        assertNotNull(processor);
        assertTrue(processor instanceof KakaoTokenProcessor);
    }

    @Test
    @DisplayName("JWT 토큰 프로세서 조회 성공")
    void testGetProcessorForJwt() {
        TokenProcessor processor = tokenProcessorFactory.getProcessor("Jwt");
        assertNotNull(processor);
        assertTrue(processor instanceof JwtTokenProcessor);
    }

    @Test
    @DisplayName("알 수 없는 토큰 타입에 대한 프로세서 조회 실패")
    void testGetProcessorForUnknownType() {
        TokenProcessor processor = tokenProcessorFactory.getProcessor("Unknown");
        assertNull(processor);
    }

    @Test
    @DisplayName("빈 프로세서 리스트로 팩토리 초기화")
    void testFactoryInitializationWithEmptyList() {
        TokenProcessorFactory emptyFactory = new TokenProcessorFactory(List.of());
        assertNull(emptyFactory.getProcessor("Google"));
        assertNull(emptyFactory.getProcessor("Kakao"));
        assertNull(emptyFactory.getProcessor("Jwt"));
    }

    @Test
    @DisplayName("중복된 프로세서로 팩토리 초기화 시 예외 발생")
    void testFactoryInitializationWithDuplicateProcessors() {
        List<TokenProcessor> processorListWithDuplicates = Arrays.asList(
                googleTokenProcessor, kakaoTokenProcessor, googleTokenProcessor
        );

        assertThrows(IllegalStateException.class, () -> new TokenProcessorFactory(processorListWithDuplicates));
    }
}