export const oauthProviders = ["github", "kakao", "google"] as const;

export type OAuthProvider = (typeof oauthProviders)[number];

export function isOAuthProvider(value: string): value is OAuthProvider {
  return (oauthProviders as readonly string[]).includes(value);
}
