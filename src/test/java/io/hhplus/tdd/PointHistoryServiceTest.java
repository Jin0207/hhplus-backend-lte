package io.hhplus.tdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointHistoryService;
import io.hhplus.tdd.point.TransactionType;

@ExtendWith(MockitoExtension.class)
public class PointHistoryServiceTest {
    @Mock
    private PointHistoryTable pointHistoryTable;

    @InjectMocks
    private PointHistoryService pointHistoryService;
    /*
     * 사용자 포인트 조회
     */
    @Test
    @DisplayName("내역이 없는 사용자의 포인트 내역정보 조회.")
    void 내역_없는_사용자_포인트_내역정보_조회() {
        // given
        Long id = 1L;
        given(pointHistoryTable.selectAllByUserId(id)).willReturn(Collections.emptyList());

        // when
        List<PointHistory> result = pointHistoryService.getHistories(id);

        // then
        assertTrue(result.isEmpty(), "조회된 내역은 빈 리스트여야 합니다.");
    }

    @Test
    @DisplayName("내역이 있는 사용자의 포인트 내역정보 조회.")
    void 내역_있는_사용자_포인트_내역정보_조회() {
       // given
        Long id = 1L;
        List<PointHistory> expectedHistories = List.of(
            new PointHistory(id, id, 500L, TransactionType.CHARGE, System.currentTimeMillis()),
            new PointHistory(id, id, 1000L, TransactionType.CHARGE, System.currentTimeMillis()),
            new PointHistory(id, id, 500L, TransactionType.USE, System.currentTimeMillis())
        );

        given(pointHistoryTable.selectAllByUserId(id)).willReturn(expectedHistories);

        // when
        List<PointHistory> result = pointHistoryService.getHistories(id);

        // then
       assertEquals(3, result.size(), "조회된 내역은 3개여야 합니다."); 
      
       assertEquals(500L, result.get(0).amount(), "첫 번째 이력의 포인트는 500원이어야 합니다.");
       assertEquals(TransactionType.CHARGE, result.get(0).type(), "첫 번째 이력은 충전이어야 합니다.");
       
       assertEquals(1000L, result.get(1).amount(), "두 번째 이력의 포인트는 1000원 충전이어야 합니다.");
       assertEquals(TransactionType.CHARGE, result.get(1).type(), "두 번째 이력은 충전이어야 합니다.");
       
       assertEquals(500L, result.get(2).amount(), "세 번째 이력의 포인트는 500원 이어야 합니다.");
       assertEquals(TransactionType.USE, result.get(2).type(), "세 번째 이력은 사용이어야 합니다.");
    }
    /**
     * 사용자 포인트 충전/사용내역
     */
    @Test
    @DisplayName("사용자가 포인트를 충전하여 포인트 히스토리를 저장한다.")
    void 포인트_충전_이력_저장() {
        // given
        Long id = 1L;
        Long chargeAmount = 2000L;

        PointHistory expected = new PointHistory(id, id, chargeAmount, TransactionType.CHARGE, System.currentTimeMillis());

        given(pointHistoryTable.insert(eq(id), eq(chargeAmount), eq(TransactionType.CHARGE), anyLong()))
            .willReturn(expected);
        // when
        PointHistory result = pointHistoryService.insertChargeHistory(id, chargeAmount);

        // then
       assertEquals(expected.amount(), result.amount(), "조회된 포인트는 Mock 설정값(2000)과 일치해야 합니다.");
       assertEquals(expected.type(), result.type(), "조회된 충전유형은 Mock 설정값(CHARGE)과 일치해야 합니다.");
    }
}
