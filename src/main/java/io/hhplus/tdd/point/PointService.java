package io.hhplus.tdd.point;

import org.springframework.stereotype.Service;

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
}
