import React from 'react';

function Footer() {
  return (
    <footer className="footer">
      <div className="footer-container">
        <p>&copy; {new Date().getFullYear()} OmniBank. All rights reserved.</p>
        <div className="footer-links">
          <a href="https://www.techtarget.com/whatis/definition/privacy-policy">Privacy Policy</a>
          <a href="https://app.termsfeed.com/wizard/privacy-policy?step=2&data=eyJhZ3JlZW1lbnRfZm9yIjpbIldlYnNpdGUiXX0%3D#step-2">Terms of Service</a>
          <a href="https://www.whatsapp.com/contact">Contact Us</a>
        </div>
      </div>
    </footer>
  );
}

export default Footer;