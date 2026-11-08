| 지표 | 0-postgres-like | 1-es-bm25 | 차이 |
|---|---|---|---|
| meanP10 | 0.1829 | 0.2171 | +0.0342 |
| mrr | 0.815 | 0.9553 | +0.1403 |
| meanNdcg10 | 0.7609 | 0.9099 | +0.1490 |
| suggestionAccuracy | 0.5 | 0.0 | -0.5000 |

**개선 14건**

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
  - 이후: p_d, p_ko1, p_sj1, p_f, p_sec
- `#spring` (feed) NDCG 0.3869 → 1.0 (+0.613)
  - 이전: f_pop, f_both, f_body
  - 이후: f_both, f_tag
- `C` (post) NDCG 0.4307 → 1.0 (+0.569)
  - 이전: p_cpp, p_cpp2, p_cs, p_c, p_en3
  - 이후: p_c, p_cpp, p_cs2, p_cs, p_cpp2
- `spring` (feed) NDCG 0.5931 → 0.9811 (+0.388)
  - 이전: f_pop, f_both, f_body
  - 이후: f_both, f_body, f_pop, f_tag, f_demo
- `kubernetes` (post) NDCG 0.7872 → 1.0 (+0.213)
  - 이전: p_k8s_en
  - 이후: p_k8s_en, p_k8s_ko
- `react` (demo) NDCG 0.7872 → 1.0 (+0.213)
  - 이전: d_react2
  - 이후: d_react2, d_react
- `react` (post) NDCG 0.8557 → 1.0 (+0.144)
  - 이전: p_react, p_rn
  - 이후: p_react, p_rn, p_react_ko
- `spring` (demo) NDCG 0.8876 → 0.9967 (+0.109)
  - 이전: d_exact, d_spring, d_pop, d_desc
  - 이후: d_spring, d_exact, d_stack, d_pop, d_desc
- `java` (post) NDCG 0.8965 → 1.0 (+0.104)
  - 이전: p_java, p_java2, p_js
  - 이후: p_java, p_java2, p_tagjava, p_f, p_sb2
- `spring` (user) NDCG 0.942 → 1.0 (+0.058)
  - 이전: u_springdev_famous, u_spring_lover, u_springdev, u_stack, u_bio
  - 이후: u_springdev, u_spring_lover, u_springdev_famous, u_bio, u_stack

**악화 7건**

- `C#` (post) NDCG 1.0 → 0.5077 (-0.492)
  - 이전: p_cs, p_cs2
  - 이후: p_c, p_cpp, p_cs2, p_cs, p_cpp2
- `spring jpa` (post) NDCG 1.0 → 0.834 (-0.166)
  - 이전: p_sj1, p_sj2
  - 이후: p_sj2, p_sj1
- `스프링` (post) NDCG 1.0 → 0.8977 (-0.102)
  - 이전: p_ko10, p_ko1, p_ko3, p_ko2, p_ko6
  - 이후: p_ko2, p_ko6, p_ko3, p_sb2, p_ko1
- `C++` (post) NDCG 1.0 → 0.9197 (-0.080)
  - 이전: p_cpp, p_cpp2
  - 이후: p_cpp, p_c, p_cpp2, p_cs2, p_cs
- `spring` (post) NDCG 0.7799 → 0.7361 (-0.044)
  - 이전: p_blocked, p_hide_me, p_f, p_sb1, p_en2
  - 이후: p_f, p_spam, p_en2, p_blocked, p_hide_me
- `spring boot` (post) NDCG 0.8138 → 0.777 (-0.037)
  - 이전: p_blocked, p_hide_me, p_sb1, p_visited, p_sb2
  - 이후: p_blocked, p_hide_me, p_visited, p_sb1, p_sb3
- `spring boot` (post) NDCG 0.6807 → 0.6565 (-0.024)
  - 이전: p_blocked, p_hide_me, p_sb1, p_visited, p_sb2
  - 이후: p_blocked, p_hide_me, p_visited, p_sb1, p_sb3
