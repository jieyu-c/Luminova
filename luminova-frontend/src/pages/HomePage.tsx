import { CapabilityStrip } from '../components/CapabilityStrip';
import { ControlSection } from '../components/ControlSection';
import { Header } from '../components/Header';
import { Hero } from '../components/Hero';
import { MetricBand } from '../components/MetricBand';
import { ProductSection } from '../components/ProductSection';
import { Showcase } from '../components/Showcase';
import { usePointerGlow } from '../hooks/usePointerGlow';

export function HomePage() {
  usePointerGlow('.home-main .btn, .case-card, .product-flow-preview .flow-node');

  return (
    <>
      <Header />
      <main className="home-main">
        <Hero />
        <ProductSection />
        <MetricBand />
        <CapabilityStrip />
        <ControlSection />
        <Showcase />
      </main>
    </>
  );
}
