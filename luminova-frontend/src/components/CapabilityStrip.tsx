import { capabilityItems } from '../data/home';

export function CapabilityStrip() {
  return (
    <section className="capability-strip" aria-label="核心能力">
      {capabilityItems.map((item) => (
        <article key={item.index}>
          <span>{item.index}</span>
          <b>{item.title}</b>
          <p>{item.text}</p>
        </article>
      ))}
    </section>
  );
}
