export interface MiniHomes {
  id: number; // PK / BIGINT
  user_id: number; // FK / BIGINT
  showcase_demo1_id: number | null; // FK / BIGINT
  showcase_demo2_id: number | null; // FK / BIGINT
  showcase_demo3_id: number | null; // FK / BIGINT
  title: string | null; // VARCHAR(50)
  intro: string | null; // VARCHAR(300)
  skin_theme: string | null; // VARCHAR(50)
  today_count: number; // INT
  total_count: number; // INT
  created_at: Date; // TIMESTAMP
  updated_at: Date; // TIMESTAMP
}

export interface BlogSubscriptions {
  id: number; // PK / BIGINT
  subscriber_id: number; // FK / BIGINT
  blog_owner_id: number; // FK / BIGINT
  paid_price: number; // INT
  status: string; // VARCHAR(20)
  started_at: Date; // TIMESTAMP
  expires_at: Date; // TIMESTAMP
}

export interface FeedComments {
  id: number; // PK / BIGINT
  feed_id: number; // FK / BIGINT
  user_id: number; // FK / BIGINT
  parent_id: number | null; // FK / BIGINT
  content: string; // VARCHAR(500)
  like_count: number; // INT
  created_at: Date; // TIMESTAMP
  updated_at: Date | null; // TIMESTAMP
  deleted_at: Date | null; // TIMESTAMP
}

export interface Reports {
  id: number; // PK / BIGINT
  reporter_id: number; // FK / BIGINT
  target_type: string; // VARCHAR(20)
  target_id: number; // BIGINT
  reason: string; // VARCHAR(500)
  status: string; // VARCHAR(20)
  handled_by: number | null; // FK / BIGINT
  admin_note: string | null; // TEXT
  created_at: Date; // TIMESTAMP
  handled_at: Date | null; // TIMESTAMP
}

export interface Settlements {
  id: number; // PK / BIGINT
  user_id: number; // FK / BIGINT
  total_chips: number; // INT
  amount_krw: number; // INT
  fee: number; // INT
  net_amount: number; // INT
  period_start: Date; // DATE
  period_end: Date; // DATE
  status: string; // VARCHAR(20)
  confirmed_by: number | null; // FK / BIGINT
  confirmed_at: Date | null; // TIMESTAMP
  created_at: Date; // TIMESTAMP
}

export interface Posts {
  id: number; // PK / BIGINT
  user_id: number; // FK / BIGINT
  demo_id: number | null; // FK / BIGINT
  title: string; // VARCHAR(200)
  content: string; // TEXT
  thumbnail_url: string | null; // VARCHAR(500)
  is_paid: boolean; // BOOLEAN
  status: string; // VARCHAR(20)
  view_count: number; // INT
  like_count: number; // INT
  created_at: Date; // TIMESTAMP
  updated_at: Date; // TIMESTAMP
  deleted_at: Date | null; // TIMESTAMP
}

export interface Follows {
  follower_id: number; // FK / BIGINT
  following_id: number; // FK / BIGINT
  created_at: Date; // TIMESTAMP
}

export interface DemoTags {
  demo_id: number; // FK / BIGINT
  tag_id: number; // FK / BIGINT
}

export interface FeedReposts {
  user_id: number; // FK / BIGINT
  feed_id: number; // FK / BIGINT
  created_at: Date; // TIMESTAMP
}

export interface Admins {
  id: number; // PK / BIGINT
  login_id: string; // VARCHAR(50)
  password: string; // VARCHAR(255)
  name: string; // VARCHAR(30)
  admin_role: string; // VARCHAR(20)
  last_login_at: Date | null; // TIMESTAMP
  created_at: Date; // TIMESTAMP
  deleted_at: Date | null; // TIMESTAMP
}

export interface AdminActionLogs {
  id: number; // PK / BIGINT
  admin_id: number; // FK / BIGINT
  action_type: string; // VARCHAR(30)
  target_type: string; // VARCHAR(20)
  target_id: number; // BIGINT
  reason: string | null; // TEXT
  created_at: Date; // TIMESTAMP
}

export interface DemoLikes {
  user_id: number; // FK / BIGINT
  demo_id: number; // FK / BIGINT
  created_at: Date; // TIMESTAMP
}

export interface Demos {
  id: number; // PK / BIGINT
  user_id: number; // FK / BIGINT
  title: string; // VARCHAR(100)
  description: string | null; // TEXT
  demo_type: string; // VARCHAR(20)
  file_path: string | null; // VARCHAR(500)
  api_url: string | null; // VARCHAR(500)
  openapi_json: string | null; // TEXT
  scenario_json: string | null; // TEXT
  external_url: string | null; // VARCHAR(500)
  thumbnail: string | null; // VARCHAR(500)
  is_paid: boolean; // BOOLEAN
  price_chips: number; // INT
  play_count: number; // INT
  like_count: number; // INT
  status: string; // VARCHAR(20)
  version: number; // INT
  created_at: Date; // TIMESTAMP
  updated_at: Date; // TIMESTAMP
  deleted_at: Date | null; // TIMESTAMP
}

export interface Blocks {
  blocker_id: number; // FK / BIGINT
  blocked_id: number; // FK / BIGINT
  created_at: Date; // TIMESTAMP
}

export interface SocialAccounts {
  id: number; // PK / BIGINT
  user_id: number; // FK / BIGINT
  provider: string; // VARCHAR(20)
  provider_id: string; // VARCHAR(100)
  created_at: Date; // TIMESTAMP
}

export interface UserTagProfile {
  user_id: number; // FK / BIGINT
  tag_id: number; // FK / BIGINT
  weight: number; // INT
  updated_at: Date; // TIMESTAMP
}

export interface DmRooms {
  id: number; // PK / BIGINT
  last_message_at: Date | null; // TIMESTAMP
  created_at: Date; // TIMESTAMP
}

export interface PostLikes {
  user_id: number; // FK / BIGINT
  post_id: number; // FK / BIGINT
  created_at: Date; // TIMESTAMP
}

export interface Feeds {
  id: number; // PK / BIGINT
  user_id: number; // FK / BIGINT
  content: string; // VARCHAR(500)
  demo_id: number | null; // FK / BIGINT
  status: string; // VARCHAR(20)
  like_count: number; // INT
  comment_count: number; // INT
  repost_count: number; // INT
  poll_options: any | null; // JSONB
  created_at: Date; // TIMESTAMP
  updated_at: Date; // TIMESTAMP
  deleted_at: Date | null; // TIMESTAMP
}

export interface UserSanctions {
  id: number; // PK / BIGINT
  user_id: number; // FK / BIGINT
  admin_id: number; // FK / BIGINT
  reason: string; // VARCHAR(500)
  starts_at: Date; // TIMESTAMP
  ends_at: Date | null; // TIMESTAMP
  released_at: Date | null; // TIMESTAMP
  created_at: Date; // TIMESTAMP
}

export interface FeedLikes {
  user_id: number; // FK / BIGINT
  feed_id: number; // FK / BIGINT
  created_at: Date; // TIMESTAMP
}

export interface Guestbooks {
  id: number; // PK / BIGINT
  home_id: number; // FK / BIGINT
  writer_id: number; // FK / BIGINT
  content: string; // VARCHAR(500)
  is_secret: boolean; // BOOLEAN
  created_at: Date; // TIMESTAMP
  deleted_at: Date | null; // TIMESTAMP
}

export interface Tags {
  id: number; // PK / BIGINT
  name: string; // VARCHAR(50)
}

export interface ChipTransactions {
  id: number; // PK / BIGINT
  user_id: number; // FK / BIGINT
  type: string; // VARCHAR(20)
  amount: number; // INT
  balance_after: number; // INT
  description: string | null; // VARCHAR(200)
  admin_id: number | null; // FK / BIGINT
  created_at: Date; // TIMESTAMP
}

export interface UserInterestTags {
  user_id: number; // FK / BIGINT
  tag_id: number; // FK / BIGINT
  created_at: Date; // TIMESTAMP
}

export interface Notifications {
  id: number; // PK / BIGINT
  receiver_id: number; // FK / BIGINT
  actor_id: number | null; // FK / BIGINT
  type: string; // VARCHAR(30)
  target_type: string | null; // VARCHAR(20)
  target_id: number | null; // BIGINT
  is_read: boolean; // BOOLEAN
  created_at: Date; // TIMESTAMP
}

export interface Bookmarks {
  user_id: number; // FK / BIGINT
  demo_id: number; // FK / BIGINT
  created_at: Date; // TIMESTAMP
}

export interface FeedVotes {
  id: number; // PK / BIGINT
  feed_id: number; // FK / BIGINT
  user_id: number; // FK / BIGINT
  option_index: number; // SMALLINT
  created_at: Date; // TIMESTAMP
}

export interface DemoFeedbacks {
  id: number; // PK / BIGINT
  demo_id: number; // FK / BIGINT
  user_id: number; // FK / BIGINT
  parent_id: number | null; // FK / BIGINT
  content: string; // TEXT
  like_count: number; // INT
  created_at: Date; // TIMESTAMP
  updated_at: Date | null; // TIMESTAMP
  deleted_at: Date | null; // TIMESTAMP
}

export interface Users {
  id: number; // PK / BIGINT
  email: string; // VARCHAR(100)
  nickname: string; // VARCHAR(30)
  profile_image: string | null; // VARCHAR(500)
  bio: string | null; // TEXT
  tech_stacks: any | null; // JSONB
  github_username: string | null; // VARCHAR(50)
  role: string; // VARCHAR(10)
  status: string; // VARCHAR(20)
  chip_balance: number; // INT
  created_at: Date; // TIMESTAMP
  updated_at: Date; // TIMESTAMP
  deleted_at: Date | null; // TIMESTAMP
}

export interface DmRoomMembers {
  room_id: number; // FK / BIGINT
  user_id: number; // FK / BIGINT
  last_read_message_id: number | null; // FK / BIGINT
  joined_at: Date; // TIMESTAMP
  left_at: Date | null; // TIMESTAMP
}

export interface PostTags {
  post_id: number; // FK / BIGINT
  tag_id: number; // FK / BIGINT
}

export interface DemoTypeConfig {
  id: number; // PK / BIGINT
  demo_id: number; // FK / BIGINT
  health_check_url: string | null; // VARCHAR(500)
  last_health_at: Date | null; // TIMESTAMP
  is_healthy: boolean; // BOOLEAN
  fallback_to_replay: boolean; // BOOLEAN
}

export interface PostComments {
  id: number; // PK / BIGINT
  post_id: number; // FK / BIGINT
  user_id: number; // FK / BIGINT
  parent_id: number | null; // FK / BIGINT
  content: string; // TEXT
  like_count: number; // INT
  created_at: Date; // TIMESTAMP
  updated_at: Date | null; // TIMESTAMP
  deleted_at: Date | null; // TIMESTAMP
}

export interface FeedTags {
  feed_id: number; // FK / BIGINT
  tag_id: number; // FK / BIGINT
}

export interface DmMessages {
  id: number; // PK / BIGINT
  room_id: number; // FK / BIGINT
  sender_id: number; // FK / BIGINT
  content: string; // VARCHAR(1000)
  created_at: Date; // TIMESTAMP
  deleted_at: Date | null; // TIMESTAMP
}

export interface FeedCommentLikes {
  user_id: number; // FK / BIGINT
  comment_id: number; // FK / BIGINT
  created_at: Date; // TIMESTAMP
}

export interface PostCommentLikes {
  user_id: number; // FK / BIGINT
  comment_id: number; // FK / BIGINT
  created_at: Date; // TIMESTAMP
}

export interface DemoFeedbackLikes {
  user_id: number; // FK / BIGINT
  feedback_id: number; // FK / BIGINT
  created_at: Date; // TIMESTAMP
}
