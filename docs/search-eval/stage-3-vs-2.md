| 지표 | 2-es-ranked | 3-es-multilingual | 차이 |
|---|---|---|---|
| meanP10 | 0.222 | 0.2317 | +0.0097 |
| mrr | 0.9878 | 0.9878 | +0.0000 |
| meanNdcg10 | 0.9397 | 0.9633 | +0.0236 |
| suggestionAccuracy | 0.0 | 1.0 | +1.0000 |

**개선 7건**

- `리액트` (post) NDCG 0.7453 → 1.0 (+0.255)
  - 이전: p_react_ko
  - 이후: p_react_ko, p_react, p_rn
- `자바스크립트` (post) NDCG 0.7872 → 1.0 (+0.213)
  - 이전: p_js2
  - 이후: p_js2, p_js, p_node_noise
- `스프링 부트` (post) NDCG 0.8035 → 0.9757 (+0.172)
  - 이전: p_ko2, p_sb2
  - 이후: p_ko2, p_sb2, p_sb1, p_visited, p_blocked
- `스프링` (post) NDCG 0.8977 → 0.997 (+0.099)
  - 이전: p_ko2, p_ko6, p_ko3, p_sb2, p_ko1
  - 이후: p_ko2, p_ko10, p_ko1, p_ko3, p_sb2
- `C#` (post) NDCG 0.9177 → 1.0 (+0.082)
  - 이전: p_cs, p_cpp, p_c, p_cpp2, p_cs2
  - 이후: p_cs, p_cs2
- `C++` (post) NDCG 0.9197 → 1.0 (+0.080)
  - 이전: p_cpp, p_cs, p_cpp2, p_c, p_cs2
  - 이후: p_cpp, p_cpp2
- `스프링으로 개발하는 방법` (post) NDCG 0.7453 → 0.8124 (+0.067)
  - 이전: p_ko10
  - 이후: p_ko10, p_ko3

**악화 0건**

