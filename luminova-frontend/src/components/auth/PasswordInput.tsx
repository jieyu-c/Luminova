import { Eye, EyeOff } from 'lucide-react';
import { forwardRef, useState } from 'react';

type PasswordInputProps = {
  id: string;
  placeholder?: string;
  autoComplete?: string;
  hasError?: boolean;
} & React.InputHTMLAttributes<HTMLInputElement>;

export const PasswordInput = forwardRef<HTMLInputElement, PasswordInputProps>(
  function PasswordInput(
    {
      id,
      placeholder = '请输入密码',
      autoComplete = 'current-password',
      hasError,
      ...props
    },
    ref,
  ) {
    const [visible, setVisible] = useState(false);

    return (
      <div className={`field-input-wrap${hasError ? ' has-error' : ''}`}>
        <input
          ref={ref}
          id={id}
          type={visible ? 'text' : 'password'}
          placeholder={placeholder}
          autoComplete={autoComplete}
          {...props}
        />
        <button
          className="field-toggle"
          type="button"
          aria-label={visible ? '隐藏密码' : '显示密码'}
          onClick={() => setVisible((v) => !v)}
        >
          {visible ? <EyeOff size={16} /> : <Eye size={16} />}
        </button>
      </div>
    );
  },
);
