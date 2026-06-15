import { useState } from 'react';
import { ArrowRight, Sparkles } from 'lucide-react';
import { Link } from 'react-router-dom';
import { heroAgentDefaultPrompt, heroAgentQuickActions } from '../data/home';
import { useAuth } from '../contexts/AuthContext';
import { cn } from '../lib/cn';

export function HeroAgentEntry() {
  const { isAuthenticated } = useAuth();
  const [prompt, setPrompt] = useState('');
  const [activeAction, setActiveAction] = useState<string | null>(null);
  const target = isAuthenticated ? '/workspace' : '/register';

  const applyQuickAction = (label: string, nextPrompt: string) => {
    setActiveAction(label);
    setPrompt(nextPrompt);
  };

  return (
    <section className="agent-quick-entry" aria-label="Agent 快捷入口">
      <div className="agent-quick-entry__bar">
        <span className="agent-quick-entry__mark" aria-hidden="true">
          <Sparkles size={15} />
        </span>
        <input
          type="text"
          value={prompt}
          placeholder={heroAgentDefaultPrompt}
          onChange={(event) => {
            setActiveAction(null);
            setPrompt(event.target.value);
          }}
          aria-label="告诉 Agent 你想创作什么"
        />
        <Link className="btn btn-primary agent-quick-entry__cta" to={target}>
          开始
          <ArrowRight size={15} aria-hidden="true" />
        </Link>
      </div>

      <div className="agent-quick-entry__hints" aria-label="快捷动作">
        {heroAgentQuickActions.map((action) => (
          <button
            key={action.label}
            type="button"
            className={cn(activeAction === action.label && 'is-active')}
            onClick={() => applyQuickAction(action.label, action.prompt)}
          >
            {action.label}
          </button>
        ))}
      </div>
    </section>
  );
}
