package io.hhplus.tdd;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;

@ExtendWith(MockitoExtension.class)
public class PointServiceTest {
    @Mock
    private UserPointTable userPointTable;
    
    @InjectMocks
    private PointService pointService;

    /**
     * 사용자 포인트 조회
     */
    @Test
    @DisplayName("보유포인트가 있는 사용자의 포인트를 조회한다.")
    void 잔액_있는_사용자_포인트_조회() {
        // given
        Long id = 1L;
        UserPoint expected = new UserPoint(id, 2000L, System.currentTimeMillis());
        given(userPointTable.selectById(id)).willReturn(expected);

        // when
        UserPoint result = pointService.getPoint(id);

        // then
       assertEquals(expected.point(), result.point(), "조회된 포인트는 Mock 설정값(2000)과 일치해야 합니다.");
    }

    @Test
    @DisplayName("보유포인트가 없는 사용자의 포인트를 조회한다.")
    void 잔액_없는_사용자_포인트_조회() {
        // given
        Long id = 1L;
        Long expected = 0L;
        given(userPointTable.selectById(id)).willReturn(UserPoint.empty(id));

        // when
        UserPoint result = pointService.getPoint(id);

        // then
        assertEquals(expected, result.point(), "조회된 포인트는 Mock 설정값(0)과 일치해야 합니다.");
    }
}