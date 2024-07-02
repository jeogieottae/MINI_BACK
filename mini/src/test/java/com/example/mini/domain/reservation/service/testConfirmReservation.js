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

const confirmItem = {
  roomId: 17,
  peopleNumber: 2,
  checkIn: '2024-06-28T14:00:00',
  checkOut: '2024-06-30T10:00:00',
};

const baseUrl = 'http://localhost:8080';

export default function () {
  let startTime = new Date().getTime();

  let itemsToSend = confirmItem || [];
  let accessToken = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlcG9pbnQxMTI2QGdtYWlsLmNvbSIsImlhdCI6MTcxOTkwNDgxMiwiZXhwIjoxNzE5OTA2NjEyfQ.YYCmA4kW8uQIWY_ksA2UKfkqPR619pvuKCg35T59lJg';
  let refreshToken = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJlcG9pbnQxMTI2QGdtYWlsLmNvbSIsImlhdCI6MTcxOTkwNDgxMiwiZXhwIjoxNzIxMTE0NDEyfQ.V3EOs3Nn5nNF1Tp7Qt87P-1F5T-w8q2xjx6lWRRdBUc';

  let confirmResponse = http.post(`${baseUrl}/api/reservation`, JSON.stringify(confirmItem), {
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
    'confirm reservation status is 200': (r) => r.status === 200,
  });

  sleep(1);
}
