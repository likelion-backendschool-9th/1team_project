// PostgreSQL과 Elasticsearch 경로를 같은 조건으로 비교하는 부하 (k6). 실행은 compare-engines.sh가 한다.
//   k6 run -e VUS=10 -e DURATION=60s compare.js
// 사용자마다 검색 후 1초 쉬고, 검색어는 VU·반복 번호로 정해서 엔진이 달라도 같은 요청 순서가 된다.
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';
const VUS = Number(__ENV.VUS || 1);
const DURATION = __ENV.DURATION || '60s';

const QUERIES = ['spring', 'react', 'docker', 'spring jpa', 'kafka 성능', '캐시', 'sprnig', 'sprng'];
const latency = new Trend('search_latency', true);

export const options = {
  scenarios: { load: { executor: 'constant-vus', vus: VUS, duration: DURATION, gracefulStop: '35s' } },
  summaryTrendStats: ['avg', 'med', 'p(95)', 'p(99)', 'max', 'count'],
};

export default function () {
  const query = QUERIES[(__VU * 7 + __ITER) % QUERIES.length];
  const res = http.get(`${BASE_URL}/api/v1/search?q=${encodeURIComponent(query)}`, { timeout: '30s' });
  check(res, { 'status 200': (r) => r.status === 200 });
  latency.add(res.timings.duration);
  sleep(1);
}
