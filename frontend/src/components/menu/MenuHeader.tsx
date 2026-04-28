import StarRating from './StarRating'
import logoPiece from '../../assets/logo.svg'
import { FaRegUserCircle } from 'react-icons/fa'
import '../../css/menu/menu-header.css'

type MenuHeaderProps = {
  averageRating: number | null
  onProfileOpen: () => void
  onLogout: () => void
}

function MenuHeader({ averageRating, onProfileOpen, onLogout }: MenuHeaderProps) {
  return (
    <header className="menu-header-line menu-card">
      <div className="brand-block">
        <h1>
          <img src={logoPiece} alt="Chess piece logo" />
        </h1>
      </div>

      <div className="header-rating">
        <span className="header-label">Average game rating</span>
        <StarRating value={averageRating} labelPrefix="Average rating" />
      </div>

      <div className="header-actions">
        <button type="button" className="profile-open-btn" onClick={onProfileOpen}>
          <FaRegUserCircle aria-hidden="true" className="profile-icon" />
          <span>Profile</span>
        </button>
        <button type="button" className="header-logout-btn" onClick={onLogout}>
          Log out
        </button>
      </div>
    </header>
  )
}

export default MenuHeader
