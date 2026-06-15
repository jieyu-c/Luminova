import { metrics } from '../data/home';

export function MetricBand() {
  return (
    <section className="metric-band" aria-label="产品指标">
      {metrics.map((item) => (
        <div key={item.label}>
          <strong>{item.value}</strong>
          <span>{item.label}</span>
          <small>{item.desc}</small>
        </div>
      ))}
    </section>
  );
}
