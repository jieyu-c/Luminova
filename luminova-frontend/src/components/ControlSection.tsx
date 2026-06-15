import { valueItems } from '../data/home';

export function ControlSection() {
  return (
    <section className="control-section" id="workflow">
      <div className="section-heading">
        <p>Product Control</p>
        <h2>产品如何帮团队保持控制感</h2>
      </div>
      <div className="value-list">
        {valueItems.map((item) => (
          <article key={item.title}>
            <b>{item.title}</b>
            <p>{item.text}</p>
          </article>
        ))}
      </div>
    </section>
  );
}
