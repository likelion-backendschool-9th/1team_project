import type { SVGProps } from "react";

type Props = SVGProps<SVGSVGElement> & {
  size?: number;
};

// 찍먹 로고: 소스 그릇에 찍힌 막대 과자. 파비콘(src/app/icon.svg)과 같은 그림이다.
// 막대는 소스 위에 그리고 소스 표면 아래를 잘라 내 소스에 잠긴 것처럼 보이게 한다.
export function LogoMark({ size = 44, ...props }: Props) {
  return (
    <svg viewBox="0 0 100 100" width={size} height={size} aria-hidden="true" focusable="false" {...props}>
      <defs>
        <clipPath id="logo-dip-surface">
          <rect width="100" height="61.5" />
        </clipPath>
      </defs>
      <rect width="100" height="100" rx="26" fill="#f5c342" />
      <path d="M15.6 61h68.8a34.4 26.6 0 0 1-68.8 0Z" fill="#1f1a17" />
      <ellipse cx="50" cy="61" rx="34.4" ry="5" fill="#e8692e" />
      <g clipPath="url(#logo-dip-surface)">
        <g transform="rotate(-20 53.5 60)">
          <rect x="42.5" y="25" width="22" height="48" rx="6" fill="#fdf6e8" stroke="#1f1a17" strokeWidth="3.2" />
          <path
            d="M48.8 37.2l-3.6 3.6 3.6 3.6M58.2 37.2l3.6 3.6-3.6 3.6"
            fill="none"
            stroke="#1f1a17"
            strokeWidth="2.6"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </g>
      </g>
      <path d="M42 64h7v4.5a3.5 3.5 0 0 1-7 0Z" fill="#e8692e" />
    </svg>
  );
}
