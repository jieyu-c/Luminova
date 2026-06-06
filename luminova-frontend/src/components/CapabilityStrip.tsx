import { capabilities } from '../data/home';

const capIcons = ['◇', '◎', '▣', '⬡'] as const;

export function CapabilityStrip() {
  return (
    <section className="capability-strip" aria-label="核心能力">
      {capabilities.map((item, index) => (
        <article className="cap-item" key={item.title}>
          <div className="cap-icon">{capIcons[index]}</div>
          <b>{item.title}</b>
          <span>{item.text}</span>
        </article>
      ))}
    </section>
  );
}
