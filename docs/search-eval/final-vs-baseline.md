| 지표 | 0-postgres-like | 4-es-personalized | 차이 |
|---|---|---|---|
| meanP10 | 0.1829 | 0.2341 | +0.0512 |
| mrr | 0.815 | 1.0 | +0.1850 |
| meanNdcg10 | 0.7609 | 0.9767 | +0.2158 |
| suggestionAccuracy | 0.5 | 1.0 | +0.5000 |

**개선 23건**

- `#spring #java` (post) NDCG 0.0 → 1.0 (+1.000)
  - 이전: (없음)
  - 이후: p_f
- `spring #java` (post) NDCG 0.0 → 1.0 (+1.000)
  - 이전: (없음)
  - 이후: p_f, p_sb2
- `c#` (demo) NDCG 0.0 → 1.0 (+1.000)
  - 이전: (없음)
  - 이후: d_omok
- `react` (feed) NDCG 0.0 → 1.0 (+1.000)
  - 이전: (없음)
  - 이후: f_react
- `#spring` (post) NDCG 0.3595 → 1.0 (+0.641)
  - 이전: p_blocked, p_hide_me, p_f, p_sb1, p_en2
  - 이후: p_sec, p_sj1, p_f, p_ko1, p_en2
- `#spring` (feed) NDCG 0.3869 → 1.0 (+0.613)
  - 이전: f_pop, f_both, f_body
  - 이후: f_both, f_tag
- `C` (post) NDCG 0.4307 → 1.0 (+0.569)
  - 이전: p_cpp, p_cpp2, p_cs, p_c, p_en3
  - 이후: p_c, p_c_noise
- `spring` (feed) NDCG 0.5931 → 0.9811 (+0.388)
  - 이전: f_pop, f_both, f_body
  - 이후: f_body, f_both, f_pop, f_tag, f_demo
- `kafka` (user) NDCG 0.6806 → 1.0 (+0.319)
  - 이전: u_kafka_z, u_kafka_y, u_kafka_x
  - 이후: u_kafka_x, u_kafka_y, u_kafka_z
- `spring boot` (post) NDCG 0.6807 → 1.0 (+0.319)
  - 이전: p_blocked, p_hide_me, p_sb1, p_visited, p_sb2
  - 이후: p_sb1, p_visited, p_sb2, p_en3, p_ko8
- `리액트` (post) NDCG 0.7453 → 1.0 (+0.255)
  - 이전: p_react_ko
  - 이후: p_react_ko, p_react, p_rn
- `자바스크립트` (post) NDCG 0.7872 → 1.0 (+0.213)
  - 이전: p_js2
  - 이후: p_js2, p_js, p_node_noise
- `kubernetes` (post) NDCG 0.7872 → 1.0 (+0.213)
  - 이전: p_k8s_en
  - 이후: p_k8s_en, p_k8s_ko
- `react` (demo) NDCG 0.7872 → 1.0 (+0.213)
  - 이전: d_react2
  - 이후: d_react2, d_react
- `JWT 토큰이 만료됐을 때 처리하는 방법` (post) NDCG 0.7453 → 0.9468 (+0.202)
  - 이전: p_ko7
  - 이후: p_ko7, p_sec
- `스프링 부트` (post) NDCG 0.8035 → 0.9757 (+0.172)
  - 이전: p_ko2, p_sb2
  - 이후: p_ko2, p_sb2, p_sb1, p_visited, p_blocked
- `spring` (post) NDCG 0.7799 → 0.9345 (+0.155)
  - 이전: p_blocked, p_hide_me, p_f, p_sb1, p_en2
  - 이후: p_f, p_sb1, p_blocked, p_visited, p_hide_me
- `react` (post) NDCG 0.8557 → 1.0 (+0.144)
  - 이전: p_react, p_rn
  - 이후: p_react, p_rn, p_react_ko
- `spring` (demo) NDCG 0.8876 → 0.9967 (+0.109)
  - 이전: d_exact, d_spring, d_pop, d_desc
  - 이후: d_exact, d_spring, d_stack, d_pop, d_desc
- `java` (post) NDCG 0.8965 → 1.0 (+0.104)
  - 이전: p_java, p_java2, p_js
  - 이후: p_java, p_java2, p_tagjava, p_f, p_sb2
- `spring boot` (post) NDCG 0.8138 → 0.9083 (+0.095)
  - 이전: p_blocked, p_hide_me, p_sb1, p_visited, p_sb2
  - 이후: p_sb1, p_visited, p_blocked, p_hide_me, p_sb2
- `스프링으로 개발하는 방법` (post) NDCG 0.7453 → 0.8124 (+0.067)
  - 이전: p_ko10
  - 이후: p_ko10, p_ko3
- `spring` (user) NDCG 0.942 → 1.0 (+0.058)
  - 이전: u_springdev_famous, u_spring_lover, u_springdev, u_stack, u_bio
  - 이후: u_spring_lover, u_springdev, u_springdev_famous, u_stack, u_bio

**악화 1건**

- `스프링` (post) NDCG 1.0 → 0.997 (-0.003)
  - 이전: p_ko10, p_ko1, p_ko3, p_ko2, p_ko6
  - 이후: p_ko2, p_ko10, p_ko1, p_ko3, p_sb2
