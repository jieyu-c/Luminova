import { CapabilityStrip } from './components/CapabilityStrip';
import { Header } from './components/Header';
import { Hero } from './components/Hero';
import { PromoStrip } from './components/PromoStrip';
import { Showcase } from './components/Showcase';

function App() {
  return (
    <div className="site-shell">
      <div className="ambient" aria-hidden="true" />
      <Header />
      <main className="landing">
        <Hero />
        <PromoStrip />
        <CapabilityStrip />
        <Showcase />
      </main>
      <div className="design-badge">概念设计稿 v2</div>
    </div>
  );
}

export default App;
