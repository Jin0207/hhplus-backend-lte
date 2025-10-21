package io.hhplus.tdd.point;

import java.util.Optional;

import org.springframework.stereotype.Service;

import io.hhplus.tdd.ErrorCode;
import io.hhplus.tdd.ErrorException;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PointService {
     private final UserPointTable userPointTable;
    /**
     * 사용자의 포인트를 조회한다.
     * @param id 사용자 아이디
     * @return UserPoint
     */
    public UserPoint getPoint(long id){
        return userPointTable.selectById(id);
    }
     /**
     * 사용자의 포인트를 충전한다.
     * @param id 사용자 아이디
     * @param ammount 충전포인트
     * @return UserPoint     
     * */
    public UserPoint chargePoint(long id, long amount){
        // 충전 금액은 0보다 작을 수 없습니다.
         Optional.ofNullable(amount >= 0 ? true : null)
            .orElseThrow(() -> new ErrorException(ErrorCode.CHARGE_LESS_THAN_ZERO));
            
        UserPoint userPoint = this.getPoint(id);
        long currentPoint = userPoint.point();
        long totalPoint = currentPoint + amount;
        
        userPoint = userPointTable.insertOrUpdate(id, totalPoint);
        return userPoint;
    }
}
