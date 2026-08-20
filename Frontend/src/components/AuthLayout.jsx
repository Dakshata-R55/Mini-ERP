import logo from '../assets/logo.png'

export default function AuthLayout({ children }) {
  return (
    <div className="auth-page">
      <div className="auth-card">
        <div className="brand">
          <img src={logo} alt="Shiv Furniture Works" />
          <h1>SHIV FURNITURE WORKS</h1>
          <p>MINI ERP · DEMAND TO DELIVERY</p>
        </div>
        {children}
      </div>
    </div>
  )
}