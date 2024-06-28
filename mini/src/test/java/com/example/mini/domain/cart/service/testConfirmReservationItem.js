import http from 'k6/http';
import { sleep, check } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// 동시 사용자 수 설정
export let options = {
  vus: 100,
  duration: '1s',
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

const confirmItem = {
  reservationId: 8,
  roomId: 16,
  peopleNumber: 2,
  checkIn: '2024-06-28T14:00:00',
  checkOut: '2024-06-30T10:00:00',
};


const baseUrl = 'http://localhost:8080';


export default function () {
  let startTime = new Date().getTime();

  let itemsToSend = confirmItem || [];
  let accessToken = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleGFtcGxlQGV4YW1wbGUuY29tIiwiaWF0IjoxNzE5NTk1MzA4LCJleHAiOjE3MTk1OTU0Mjh9.HurWx-cH_HclMFjs9-XJ3W7tYPkX1zaBl5NVSBdReMI';
  let refreshToken = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJleGFtcGxlQGV4YW1wbGUuY29tIiwiaWF0IjoxNzE5NTk1MzA4LCJleHAiOjE3MjA4MDQ5MDh9.2cwWcUk86aou6_xdqqOmuLcdB90HJt-BlFD_NZ2VtOU';

  let confirmResponse = http.put(`${baseUrl}/api/cart`, JSON.stringify(confirmItem), {
    headers: {
      'Content-Type': 'application/json',
      'Cookie': `accessToken=${accessToken}; refreshToken=${refreshToken}`
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


  sleep(1);
}
