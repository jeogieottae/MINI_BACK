import http from 'k6/http';
import { sleep, check } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// 동시 사용자 수 설정
export let options = {
  vus: 100,
  duration: '30s',
};

// API 응답 시간
let apiResponseTime = new Trend('api_response_time');

// Lock acquisition time
let lockAcquisitionTime = new Trend('lock_acquisition_time');

// Lock 실패율
let lockFailureRate = new Rate('lock_failure_rate');

// Redisson 락 성공 횟수
let redissonLocksAcquired = new Trend('redisson_locks_acquired');

// Redisson 락 실패 횟수
let redissonLocksFailed = new Trend('redisson_locks_failed');

const confirmItemstest = [
  {
    roomId: 101,
    peopleNumber: 2,
    checkIn: '2024-06-28T14:00:00',
    checkOut: '2024-06-30T10:00:00',
  },
  {
    roomId: 102,
    peopleNumber: 3,
    checkIn: '2024-07-01T15:00:00',
    checkOut: '2024-07-04T12:00:00',
  }
];


const baseUrl = 'http://localhost:8080';


export default function () {
  let startTime = new Date().getTime();

  // Redisson 락 획득 시도
  let lockAcquired = false;
  try {
    // 여기서는 단순히 획득 시도를 시뮬레이션, 성공/실패를 랜덤하게 설정
    if (Math.random() < 0.8) { // 80%의 확률로 락 획득 성공
      redissonLocksAcquired.add(1);
      lockAcquired = true;
    } else {
      redissonLocksFailed.add(1);
    }
  } catch (e) {
    redissonLocksFailed.add(1);
  }

  // 락 획득에 성공했을 경우 API 호출 수행
  if (lockAcquired) {
    let itemsToSend = confirmItemstest || [];

    let confirmResponse = http.put(`${baseUrl}/api/cart/test`, JSON.stringify({
      confirmItemstest: itemsToSend,
    }), {
      headers: {
        'Content-Type': 'application/json',
      },
    });

    // API 응답 시간을 Trend에 추가
    apiResponseTime.add(confirmResponse.timings.duration);

    // Lock acquisition 시간을 Trend에 추가
    let endTime = new Date().getTime();
    let lockTime = endTime - startTime;
    lockAcquisitionTime.add(lockTime);

    // Lock 실패율을 Rate에 추가
    if (confirmResponse.status !== 200) {
      lockFailureRate.add(1);
    }

    // API 호출 결과를 검증
    check(confirmResponse, {
      'confirm cart items status is 200': (r) => r.status === 200,
    });
  }

  sleep(1);
}
