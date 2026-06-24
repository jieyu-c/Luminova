import { useEffect, useState } from 'react';
import { cn } from '../../lib/cn';

type UserAvatarProps = {
  username?: string | null;
  avatarUrl?: string | null;
  className?: string;
  decorative?: boolean;
};

function avatarLabel(username: string) {
  const normalized = username.trim();
  if (!normalized) return '用户';

  const words = normalized.split(/[\s_-]+/).filter(Boolean);
  if (words.length > 1) {
    return words
      .slice(0, 2)
      .map((word) => Array.from(word)[0])
      .join('')
      .toUpperCase();
  }

  return Array.from(normalized).slice(0, 2).join('').toUpperCase();
}

export function UserAvatar({
  username,
  avatarUrl,
  className,
  decorative = false,
}: UserAvatarProps) {
  const displayName = username?.trim() || '用户';
  const [imageFailed, setImageFailed] = useState(false);

  useEffect(() => {
    setImageFailed(false);
  }, [avatarUrl]);

  return (
    <span
      className={cn('user-avatar', className)}
      aria-hidden={decorative || undefined}
      role={decorative ? undefined : 'img'}
      aria-label={decorative ? undefined : `${displayName}的头像`}
    >
      {avatarUrl && !imageFailed ? (
        <img src={avatarUrl} alt="" onError={() => setImageFailed(true)} />
      ) : (
        <span className="user-avatar__label">{avatarLabel(displayName)}</span>
      )}
    </span>
  );
}
