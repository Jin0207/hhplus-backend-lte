package io.hhplus.tdd.point;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.database.PointHistoryTable;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointHistoryService {
    private final PointHistoryTable pointHistoryTable;
    private final UserLockManager userLockManager;
    /**
     * 사용자의 포인트 충전/이용 내역을 조회한다.
     * @param id 사용자 아이디
     * @return PointHistory
     */
    public List<PointHistory> getHistories(long id){
        return pointHistoryTable.selectAllByUserId(id);
    }
    /**
     * 사용자의 포인트 충전이력을 저장한다.
     * @param id 사용자아이디
     * @param amount 충전포인트
     * @return PointHistory    
     * */
    public PointHistory insertChargeHistory(long id, long amount){
        ReentrantLock lock = userLockManager.getLock(id);
        lock.lock();

        try{
            return pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
        }finally{
            lock.unlock();
        }
    }
    /**
     * 사용자의 포인트 사용이력을 저장한다.
     * @param id 사용자아이디
     * @param amount 사용포인트
     * @return PointHistory    
     * */
    public PointHistory insertUseHistory(long id, long amount){
        ReentrantLock lock = userLockManager.getLock(id);
        lock.lock();

        try{
            return pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());
        }finally{
            lock.unlock();
        }
    }
}
