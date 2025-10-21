package io.hhplus.tdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
    
}
