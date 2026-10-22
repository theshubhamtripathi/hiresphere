import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import toast from 'react-hot-toast';
import { fetchPlans, fetchMySubscription, createOrder, verifyPayment } from '../../features/payment/paymentSlice';
import { Spinner, SectionTitle } from '../../components/ui';

function loadRazorpay() {
  return new Promise((resolve) => {
    if (window.Razorpay) return resolve(true);
    const script = document.createElement('script');
    script.src = 'https://checkout.razorpay.com/v1/checkout.js';
    script.onload = () => resolve(true);
    script.onerror = () => resolve(false);
    document.body.appendChild(script);
  });
}

export default function Billing() {
  const dispatch = useDispatch();
  const { plans, subscription } = useSelector((s) => s.payment);

  useEffect(() => {
    dispatch(fetchPlans());
    dispatch(fetchMySubscription());
  }, [dispatch]);

  const upgrade = async (planCode) => {
    try {
      const order = await dispatch(createOrder(planCode)).unwrap();
      const loaded = await loadRazorpay();
      if (!loaded) return toast.error('Could not load checkout');
      const rzp = new window.Razorpay({
        key: order.keyId,
        amount: Number(order.amount) * 100,
        currency: order.currency,
        name: 'HireSphere',
        description: `${planCode} plan`,
        order_id: order.razorpayOrderId,
        handler: async (resp) => {
          try {
            await dispatch(verifyPayment({
              razorpayOrderId: resp.razorpay_order_id,
              razorpayPaymentId: resp.razorpay_payment_id,
              razorpaySignature: resp.razorpay_signature,
            })).unwrap();
            toast.success('Payment successful! Plan upgraded.');
          } catch {
            toast.error('Payment verification failed');
          }
        },
      });
      rzp.open();
    } catch (e) {
      if (e.message?.includes('503')) toast.error('Payments are not configured on this instance.');
      else toast.error('Could not start checkout');
    }
  };

  if (!plans.length) return <Spinner />;

  return (
    <div>
      <SectionTitle title="Billing & Plans" />
      {subscription && (
        <div className="card mb-6 p-5">
          <p className="text-sm text-slate-500">Current plan</p>
          <p className="text-2xl font-bold text-brand-600">{subscription.planCode}</p>
          <p className="mt-1 text-sm text-slate-600">{subscription.creditsLeft} job credit(s) remaining</p>
        </div>
      )}
      <div className="grid gap-5 md:grid-cols-3">
        {plans.map((p) => {
          const current = subscription?.planCode === p.code;
          return (
            <div key={p.code} className={`card flex flex-col p-6 ${current ? 'ring-2 ring-brand-500' : ''}`}>
              <h3 className="text-lg font-bold text-slate-900">{p.name}</h3>
              <p className="mt-2 text-3xl font-bold text-slate-900">
                {p.priceInr > 0 ? `₹${Number(p.priceInr).toLocaleString()}` : 'Free'}
                {p.priceInr > 0 && <span className="text-sm font-normal text-slate-400">/mo</span>}
              </p>
              <p className="mt-3 text-sm text-slate-500">{p.description}</p>
              <ul className="mt-4 flex-1 space-y-1 text-sm text-slate-600">
                <li>✓ {p.jobCredits} job credits</li>
                <li>✓ {p.featuredListings} featured listings</li>
              </ul>
              <button className={`mt-5 ${current ? 'btn-outline' : 'btn-primary'}`} disabled={current || p.priceInr === 0}
                onClick={() => upgrade(p.code)}>
                {current ? 'Current plan' : p.priceInr === 0 ? 'Included' : 'Upgrade'}
              </button>
            </div>
          );
        })}
      </div>
    </div>
  );
}
