// 통합 검색 부하 테스트 (k6). 로컬 서버에만 건다. 실행은 load-test.sh가 시나리오별로 나눠서 한다.
//   k6 run -e SCENARIO=s1 -e CASE=hit load.js
// 환경변수
//   BASE_URL  서버 주소 (기본 http://localhost:8081)
//   SCENARIO  s1(기준선: 사용자 1명이 같은 요청 반복) / s2(타이핑: 한 글자씩 자동완성 후 검색) / s3(동시 사용자 증가)
//   CASE      s1에서 잴 요청 (아래 CASES의 이름)
//   VUS       s2 동시 사용자 수 (기본 10), MAX_VUS s3 최대 사용자 수 (기본 50)
import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8081';
const SCENARIO = __ENV.SCENARIO || 's1';

// s1에서 잴 요청. 검색 결과 수에 따라 서버가 타는 경로가 다르다
const CASES = {
  hit: '/api/v1/search?q=spring',            // 결과 많음: 검색 4개만
  multi: '/api/v1/search?q=spring%20jpa',    // 여러 단어
  typo: '/api/v1/search?q=sprnig',           // 0건: 검색 → 교정 후보 → 고친 검색어로 다시 검색
  few: '/api/v1/search?q=sprng',             // 1~4건: 검색 → 제안 후보 → 제안 검색어 확인 검색
  ac: '/api/v1/search/autocomplete?q=spr',   // 자동완성 영어
  acKo: '/api/v1/search/autocomplete?q=%EC%8A%A4%ED%94%8C', // 자동완성 한글 조합 중(스플)
};

// s2: 검색창에 치는 순서. 한글은 입력창에 실제로 보이는 중간 상태 그대로
const TYPING = [
  ['s', 'sp', 'spr', 'spri', 'sprin', 'spring', 'spring ', 'spring b', 'spring bo', 'spring boo', 'spring boot'],
  ['ㅅ', '스', '슾', '스프', '스플', '스프리', '스프링'],
  ['d', 'do', 'doc', 'dock', 'docke', 'docker'],
  ['r', 're', 'rea', 'reac', 'react'],
];
// s3: 사용자가 검색하는 검색어. 결과 많음 / 여러 단어 / 0건(교정) / 1~4건(제안)을 섞는다
const QUERIES = ['spring', 'react', 'docker', 'spring jpa', 'kafka 성능', '캐시', 'sprnig', 'sprng'];

const latency = {
  search: new Trend('search_latency', true),
  autocomplete: new Trend('autocomplete_latency', true),
};

const scenarios = {
  s1: { executor: 'per-vu-iterations', vus: 1, iterations: Number(__ENV.ITERATIONS || 30), maxDuration: '10m' },
  s2: { executor: 'constant-vus', vus: Number(__ENV.VUS || 10), duration: __ENV.DURATION || '60s' },
  s3: {
    executor: 'ramping-vus',
    startVUs: 1,
    stages: [
      { duration: '20s', target: 10 },
      { duration: '30s', target: 10 },
      { duration: '20s', target: Number(__ENV.MAX_VUS || 50) },
      { duration: '30s', target: Number(__ENV.MAX_VUS || 50) },
      { duration: '10s', target: 0 },
    ],
    gracefulRampDown: '30s',
  },
};

export const options = {
  scenarios: { [SCENARIO]: scenarios[SCENARIO] },
  // 6단계 목표로 둔 기준. 넘으면 k6가 실패로 표시한다 (측정은 그대로 끝까지 한다)
  thresholds: {
    http_req_failed: ['rate<0.01'],
    search_latency: ['p(95)<500'],
    autocomplete_latency: ['p(95)<100'],
  },
  summaryTrendStats: ['avg', 'med', 'p(95)', 'p(99)', 'max', 'count'],
  // 데이터가 크면 데우기 요청만으로 기본 제한(60초)을 넘는다
  setupTimeout: '10m',
};

function get(kind, path) {
  const res = http.get(BASE_URL + path, { tags: { kind } });
  check(res, { 'status 200': (r) => r.status === 200 });
  latency[kind].add(res.timings.duration);
  return res;
}

// 서버·DB 캐시를 데우는 요청. 결과에 넣지 않는다
export function setup() {
  const paths = SCENARIO === 's1' ? [CASES[__ENV.CASE]] : Object.values(CASES);
  for (let i = 0; i < 3; i++) {
    paths.forEach((path) => http.get(BASE_URL + path, { tags: { kind: 'warmup' } }));
  }
}

export default function () {
  if (SCENARIO === 's1') {
    const path = CASES[__ENV.CASE];
    get(path.includes('autocomplete') ? 'autocomplete' : 'search', path);
    return;
  }
  if (SCENARIO === 's2') {
    const keys = TYPING[(__VU + __ITER) % TYPING.length];
    // 한 글자에 0.3초. 프론트 debounce(250ms)보다 느리게 치면 글자마다 요청이 간다(가장 요청이 많은 경우)
    keys.forEach((typed) => {
      get('autocomplete', '/api/v1/search/autocomplete?q=' + encodeURIComponent(typed));
      sleep(0.3);
    });
    get('search', '/api/v1/search?q=' + encodeURIComponent(keys[keys.length - 1]));
    sleep(1);
    return;
  }
  const query = QUERIES[(__VU * 7 + __ITER) % QUERIES.length];
  get('search', '/api/v1/search?q=' + encodeURIComponent(query));
  sleep(1); // 결과를 읽는 시간
}
