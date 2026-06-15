import { brandAssets } from '../../data/brand';

type LuminovaMarkProps = {
  className?: string;
  size?: number;
  variant?: 'default' | 'dark';
};

export function LuminovaMark({ className, size = 34, variant = 'default' }: LuminovaMarkProps) {
  const src = variant === 'dark' ? brandAssets.logoMarkDark : brandAssets.logoMark;

  return (
    <img
      className={className}
      src={src}
      width={size}
      height={size}
      alt=""
      aria-hidden="true"
      decoding="async"
    />
  );
}
