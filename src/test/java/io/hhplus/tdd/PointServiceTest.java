package io.hhplus.tdd;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    /**
     * 사용자 포인트 충전
     */
    @Test
    @DisplayName("사용자가 포인트를 충전한다.")
    void 포인트_충전() {
        // given
        Long id = 1L;
        Long chargeAmount = 2000L;

        UserPoint current = new UserPoint(id, 0L, System.currentTimeMillis());
        UserPoint expected = new UserPoint(id, 0L, System.currentTimeMillis());
  
        given(userPointTable.selectById(id)).willReturn(current);
        given(userPointTable.insertOrUpdate(id, chargeAmount)).willReturn(expected);

        // when
        UserPoint result = pointService.chargePoint(id, chargeAmount);

        // then
       assertEquals(expected.point(), result.point(), "조회된 포인트는 Mock 설정값(2000)과 일치해야 합니다.");
    }

    @Test
    @DisplayName("포인트 충전 금액이 0보다 작으면 예외를 던진다.")
    void 포인트_충전_실패() {
        // given
        Long id = 1L;
        Long chargeAmount = -1000L;

        // when & then
        ErrorException exception = assertThrows(ErrorException.class, () -> {
            pointService.chargePoint(id, chargeAmount);
        });
        
        // then
        assertEquals(ErrorCode.CHARGE_LESS_THAN_ZERO, exception.getErrorCode());
        assertEquals("충전 금액은 0보다 작을 수 없습니다.", exception.getMessage()); 
    }
    /**
     * 사용자 포인트 사용
     */
    @Test
    @DisplayName("사용자가 포인트를 사용한다.")
    void 포인트_사용() {
        // given
        Long id = 1L;
        Long useAmount = 3000L;
        Long remainAmount = 2000L;

        UserPoint current = new UserPoint(id, 5000L, System.currentTimeMillis());
        UserPoint expected = new UserPoint(id, 2000L, System.currentTimeMillis());
  
        given(userPointTable.selectById(id)).willReturn(current);
        given(userPointTable.insertOrUpdate(id, remainAmount)).willReturn(expected);

        // when
        UserPoint result = pointService.usePoint(id, useAmount);

        // then
       assertEquals(expected.point(), result.point(), "조회된 포인트는 Mock 설정값(2000)과 일치해야 합니다.");
    }

    @Test
    @DisplayName("포인트 사용 금액이 0보다 작으면 예외를 던진다.")
    void 포인트_사용_실패_음수() {
        // given
        Long id = 1L;
        Long useAmount = -1000L;

        UserPoint current = new UserPoint(id, 2000L, System.currentTimeMillis());
  
        given(userPointTable.selectById(id)).willReturn(current);

        // when & then
        ErrorException exception = assertThrows(ErrorException.class, () -> {
            pointService.usePoint(id, useAmount);
        });
        
        // then
        assertEquals(ErrorCode.POINT_USE_LESS_THAN_ZERO, exception.getErrorCode());
        assertEquals("포인트 사용금액은 0보다 작을 수 없습니다.", exception.getMessage()); 
    }

    @Test
    @DisplayName("포인트 사용 금액이 보유포인트보다 크면 예외를 던진다.")
    void 포인트_사용_실패_잔여_포인트_적음() {
        // given
        Long id = 1L;
        Long useAmount = 3000L;

        UserPoint current = new UserPoint(id, 2000L, System.currentTimeMillis());
  
        given(userPointTable.selectById(id)).willReturn(current);

        // when & then
        ErrorException exception = assertThrows(ErrorException.class, () -> {
            pointService.usePoint(id, useAmount);
        });
        
        // then
        assertEquals(ErrorCode.POINT_USE_MORE_THAN_REMAIN, exception.getErrorCode());
        assertEquals("포인트 사용 금액이 보유 포인트보다 많을 수 없습니다.", exception.getMessage()); 
    }
}