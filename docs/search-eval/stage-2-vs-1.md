| 지표 | 1-es-bm25 | 2-es-ranked | 차이 |
|---|---|---|---|
| meanP10 | 0.2171 | 0.222 | +0.0049 |
| mrr | 0.9553 | 0.9878 | +0.0325 |
| meanNdcg10 | 0.9099 | 0.9397 | +0.0298 |
| suggestionAccuracy | 0.0 | 0.0 | +0.0000 |

**개선 6건**

- `C#` (post) NDCG 0.5077 → 0.9177 (+0.410)
  - 이전: p_c, p_cpp, p_cs2, p_cs, p_cpp2
  - 이후: p_cs, p_cpp, p_c, p_cpp2, p_cs2
- `spring boot` (post) NDCG 0.6565 → 0.9182 (+0.262)
  - 이전: p_blocked, p_hide_me, p_visited, p_sb1, p_sb3
  - 이후: p_sb1, p_visited, p_blocked, p_hide_me, p_sb2
- `spring` (post) NDCG 0.7361 → 0.9345 (+0.198)
  - 이전: p_f, p_spam, p_en2, p_blocked, p_hide_me
  - 이후: p_f, p_sb1, p_blocked, p_visited, p_hide_me
- `spring jpa` (post) NDCG 0.834 → 1.0 (+0.166)
  - 이전: p_sj2, p_sj1
  - 이후: p_sj1, p_sj2
- `spring boot` (post) NDCG 0.777 → 0.9083 (+0.131)
  - 이전: p_blocked, p_hide_me, p_visited, p_sb1, p_sb3
  - 이후: p_sb1, p_visited, p_blocked, p_hide_me, p_sb2
- `kafka` (user) NDCG 0.6806 → 0.7364 (+0.056)
  - 이전: u_kafka_z, u_kafka_y, u_kafka_x
  - 이후: u_kafka_z, u_kafka_x, u_kafka_y

**악화 0건**

