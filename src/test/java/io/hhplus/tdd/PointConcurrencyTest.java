package io.hhplus.tdd;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointHistory;
import io.hhplus.tdd.point.PointHistoryService;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.TransactionType;
import io.hhplus.tdd.point.UserLockManager;
import io.hhplus.tdd.point.UserPoint;

public class PointConcurrencyTest {
    private final UserPointTable userPointTable = new UserPointTable();
    private final PointHistoryTable userHistoryTable = new PointHistoryTable();
    private final UserLockManager userLockManager = new UserLockManager();
    private final PointService pointService = new PointService(userPointTable, userLockManager);
    private final PointHistoryService pointHistoryService = new PointHistoryService(userHistoryTable, userLockManager);

    /*
     * 포인트 충전후 사용 동시성 테스트
     * 동시에 여러 요청이 들어오면 순차적으로 포인트 충전/사용을 실행한다.
     */
    @Test
    @DisplayName("동시에 여러 사용자가 포인트를 충전후 사용하더라도 순차적으로 처리된다.")
    void 동시성_충전후_사용_테스트() throws InterruptedException {
        long[] ids = {1L, 2L, 3L, 4L, 5L};
        long[] chargeAmounts = {1000L, 2000L, 3000L, 4000L, 5000L};
        long[] useAmounts = {100L, 200L, 300L, 400L, 500L};

        int threadCount = ids.length; //5명 동시시도

        /*
            충전+사용 쓰레드 각 5개이므로 2배값으로 쓰레드 고정
            - 충전 스레드가 끝나기 전 사용 스레드가 동시에 접근하게 되면 스레드 풀 여유없어 블로킹 가능성
            - 동시에 useLatch.await() 호출되면 deadlock 발생가능성
        */
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount * 2);
        // when
        // 포인트 충전 스레드
        CountDownLatch chargeLatch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.execute(() -> {
                try {
                    pointService.chargePoint(ids[index], chargeAmounts[index]);
                    pointHistoryService.insertChargeHistory(ids[index], chargeAmounts[index]);
                } finally {
                    chargeLatch.countDown();
                }
            });
        }
        chargeLatch.await(); //종료 대기

        // 포인트 사용 스레드
        CountDownLatch useLatch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.execute(() -> {
                try {
                    pointService.usePoint(ids[index], useAmounts[index]);
                    pointHistoryService.insertUseHistory(ids[index], useAmounts[index]);
                } finally {
                    useLatch.countDown();
                }
            });
        }

        useLatch.await(); //종료 대기
        executorService.shutdown();
        // 모든 스레드 완료 보장
        executorService.awaitTermination(1, TimeUnit.MINUTES);


        // then
        for(int i = 0; i < threadCount; i++){
            UserPoint resultUser = pointService.getPoint(ids[i]);
            List<PointHistory> resultUserHistory = pointHistoryService.getHistories(ids[i]);
            Long expectedPoint = chargeAmounts[i] - useAmounts[i];
            
            // UserPoint
            assertEquals(ids[i], resultUser.id(), i + "번째 id는 " + resultUser.id() + "가 아닌" + ids[i] + "이어야 합니다.");
            assertEquals(expectedPoint, resultUser.point(), i + "번째point는 " +  resultUser.point() + "원이 아닌" + expectedPoint + "이어야 합니다.");
            
            // PointHistory
            // 1.충전
            assertEquals(ids[i], resultUserHistory.get(0).userId(), i + "번째 히스토리 id는 " + resultUser.id() + "가 아닌" + ids[i] + "이어야 합니다.");
            assertEquals(chargeAmounts[i], resultUserHistory.get(0).amount(), i + "번째 히스토리 point는 " +  resultUser.point() + "원이 아닌" + chargeAmounts[i] + "이어야 합니다.");
            assertEquals(TransactionType.CHARGE, resultUserHistory.get(0).type(), i + "번째 히스토리 type은 " +  resultUserHistory.get(0).type() + "이 아닌 'CHARGE'여야 합니다.");
            // 2.사용
            assertEquals(useAmounts[i], resultUserHistory.get(1).amount(), i + "번째 히스토리 point는 " +  resultUser.point() + "원이 아닌" + useAmounts[i] + "이어야 합니다.");
            assertEquals(TransactionType.USE, resultUserHistory.get(1).type(), i + "번째 히스토리 type은 " +  resultUserHistory.get(0).type() + "이 아닌 'USE'여야 합니다.");
        }
    }

    /*
     * 포인트 충전 동시성 테스트
     * 동시에 여러 요청이 들어오면 순차적으로 포인트 충전을 실행한다.
     */
    @Test
    @DisplayName("동시에 여러 사용자가 포인트를 충전하더라도 순차적으로 처리된다.")
    void 동시성_충전() throws InterruptedException {
        // given
        long[] ids = {1L, 2L, 3L, 4L, 5L};
        long[] chargeAmounts = {1000L, 2000L, 3000L, 4000L, 50000L};
        int threadCount = ids.length; //5명 동시시도

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // when
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executorService.execute(() -> {
                try {
                    pointService.chargePoint(ids[index], chargeAmounts[index]);
                    pointHistoryService.insertChargeHistory(ids[index], chargeAmounts[index]);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); //종료 대기
        executorService.shutdown();

        // then
        for(int i = 0; i < threadCount; i++){
            UserPoint resultUser = pointService.getPoint(ids[i]);
            List<PointHistory> resultUserHistory = pointHistoryService.getHistories(ids[i]);

            //resultUser
            assertEquals(ids[i], resultUser.id(), i + "번째 id는 " + resultUser.id() + "가 아닌" + ids[i] + "이어야 합니다.");
            assertEquals(chargeAmounts[i], resultUser.point(), i + "번째point는 " +  resultUser.point() + "원이 아닌" + chargeAmounts[i] + "이어야 합니다.");
            //resultUserHistory
            assertEquals(ids[i], resultUserHistory.get(0).userId(), i + "번째 히스토리 id는 " + resultUser.id() + "가 아닌" + ids[i] + "이어야 합니다.");
            assertEquals(chargeAmounts[i], resultUserHistory.get(0).amount(), i + "번째 히스토리 point는 " +  resultUser.point() + "원이 아닌" + chargeAmounts[i] + "이어야 합니다.");
        }
    }
}
