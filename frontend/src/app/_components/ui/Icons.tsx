import type { ReactNode, SVGProps } from "react";

type Props = SVGProps<SVGSVGElement>;

function Svg({ children, ...rest }: Props & { children: ReactNode }) {
  return (
    <svg
      width="1em"
      height="1em"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      {...rest}
    >
      {children}
    </svg>
  );
}

export function GridIcon(props: Props) {
  return (
    <Svg {...props}>
      <rect x="3.5" y="3.5" width="7" height="7" rx="2" />
      <rect x="13.5" y="3.5" width="7" height="7" rx="2" />
      <rect x="3.5" y="13.5" width="7" height="7" rx="2" />
      <rect x="13.5" y="13.5" width="7" height="7" rx="2" />
    </Svg>
  );
}

export function BookIcon(props: Props) {
  return (
    <Svg {...props}>
      <path d="M12 6.5C10.5 5 8 4.5 4 4.5v13c4 0 6.5.5 8 2 1.5-1.5 4-2 8-2v-13c-4 0-6.5.5-8 2Z" />
      <path d="M12 6.5v13" />
    </Svg>
  );
}

export function NewsIcon(props: Props) {
  return (
    <Svg {...props}>
      <rect x="4" y="3.5" width="16" height="17" rx="2.5" />
      <path d="M8 8.5h8M8 12h8M8 15.5h5" />
    </Svg>
  );
}

export function TicketIcon(props: Props) {
  return (
    <Svg {...props}>
      <path d="M4 8.5a2 2 0 0 0 0 4v0a2 2 0 0 1 0 4V18a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-1.5a2 2 0 0 1 0-4v0a2 2 0 0 0 0-4V7a1 1 0 0 0-1-1H5a1 1 0 0 0-1 1Z" />
      <path d="M9 10h6M9 14h6" />
    </Svg>
  );
}

export function SearchIcon(props: Props) {
  return (
    <Svg {...props}>
      <circle cx="11" cy="11" r="6.5" />
      <path d="m16 16 4 4" />
    </Svg>
  );
}

export function HeartIcon({ filled, ...props }: Props & { filled?: boolean }) {
  return (
    <Svg {...props} fill={filled ? "currentColor" : "none"}>
      <path d="M12 20s-7.5-4.6-7.5-10.2A4.3 4.3 0 0 1 12 7.3a4.3 4.3 0 0 1 7.5 2.5C19.5 15.4 12 20 12 20Z" />
    </Svg>
  );
}

export function CommentIcon(props: Props) {
  return (
    <Svg {...props}>
      <path d="M20 11.5c0 3.6-3.6 6.5-8 6.5-1 0-1.9-.1-2.8-.4L4.5 19.5l1.2-3.6C4.6 14.8 4 13.2 4 11.5 4 7.9 7.6 5 12 5s8 2.9 8 6.5Z" />
    </Svg>
  );
}

export function RepostIcon(props: Props) {
  return (
    <Svg {...props}>
      <path d="M4.5 12a7.5 7.5 0 0 1 12.6-5.5L19.5 9M19.5 4.5V9H15" />
      <path d="M19.5 12a7.5 7.5 0 0 1-12.6 5.5L4.5 15M4.5 19.5V15H9" />
    </Svg>
  );
}

export function BookmarkIcon({ filled, ...props }: Props & { filled?: boolean }) {
  return (
    <Svg {...props} fill={filled ? "currentColor" : "none"}>
      <path d="M6.5 4.5h11v15L12 15.5l-5.5 4Z" />
    </Svg>
  );
}

export function ArrowRightIcon(props: Props) {
  return (
    <Svg {...props}>
      <path d="M5 12h14M13 6l6 6-6 6" />
    </Svg>
  );
}

export function ChevronRightIcon(props: Props) {
  return (
    <Svg {...props}>
      <path d="m9 6 6 6-6 6" />
    </Svg>
  );
}

export function UserIcon(props: Props) {
  return (
    <Svg {...props}>
      <circle cx="12" cy="8.5" r="3.7" />
      <path d="M4.8 19.5c.9-3.4 3.9-5 7.2-5s6.300 1.600 7.200 5" />
    </Svg>
  );
}

export function BellIcon(props: Props) {
  return (
    <Svg {...props}>
      <path d="M6 16.5v-5a6 6 0 0 1 12 0v5l1.500 2h-15Z" />
      <path d="M10 20.5a2.200 2.200 0 0 0 4 0" />
    </Svg>
  );
}

export function GearIcon(props: Props) {
  return (
    <Svg {...props}>
      <circle cx="12" cy="12" r="3" />
      <path d="M12 3.5v2.200M12 18.300v2.200M3.500 12h2.200M18.300 12h2.200M6 6l1.600 1.600M16.400 16.400 18 18M18 6l-1.600 1.600M7.600 16.400 6 18" />
    </Svg>
  );
}

export function PlusIcon(props: Props) {
  return (
    <Svg {...props}>
      <path d="M12 5v14M5 12h14" />
    </Svg>
  );
}

export function ImageIcon(props: Props) {
  return (
    <Svg {...props}>
      <rect x="3.500" y="4.500" width="17" height="15" rx="2.500" />
      <circle cx="9" cy="10" r="1.500" />
      <path d="m4 17 4.500-4.500 3.500 3.500 3-3 5 5" />
    </Svg>
  );
}

export function LinkIcon(props: Props) {
  return (
    <Svg {...props}>
      <path d="M10 14a4 4 0 0 0 5.700 0l3-3a4 4 0 0 0-5.700-5.700l-1 1" />
      <path d="M14 10a4 4 0 0 0-5.700 0l-3 3A4 4 0 0 0 11 18.700l1-1" />
    </Svg>
  );
}

export function MoreIcon(props: Props) {
  return (
    <Svg {...props} fill="currentColor" stroke="none">
      <circle cx="12" cy="5.500" r="1.700" />
      <circle cx="12" cy="12" r="1.700" />
      <circle cx="12" cy="18.500" r="1.700" />
    </Svg>
  );
}

export function DotsIcon(props: Props) {
  return (
    <Svg {...props} fill="currentColor" stroke="none">
      <circle cx="5.500" cy="12" r="1.700" />
      <circle cx="12" cy="12" r="1.700" />
      <circle cx="18.500" cy="12" r="1.700" />
    </Svg>
  );
}
