| 지표 | 3-es-multilingual | 4-es-personalized | 차이 |
|---|---|---|---|
| meanP10 | 0.2341 | 0.2341 | +0.0000 |
| mrr | 0.9878 | 1.0 | +0.0122 |
| meanNdcg10 | 0.9682 | 0.9767 | +0.0085 |
| suggestionAccuracy | 1.0 | 1.0 | +0.0000 |

**개선 2건**

- `kafka` (user) NDCG 0.7364 → 1.0 (+0.264)
  - 이전: u_kafka_z, u_kafka_x, u_kafka_y
  - 이후: u_kafka_x, u_kafka_y, u_kafka_z
- `spring boot` (post) NDCG 0.9182 → 1.0 (+0.082)
  - 이전: p_sb1, p_visited, p_blocked, p_hide_me, p_sb2
  - 이후: p_sb1, p_visited, p_sb2, p_en3, p_ko8

**악화 0건**

