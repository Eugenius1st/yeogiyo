// import { AiOutlineCloseCircle } from "react-icons/ai";
import { useState } from "react";
import { Link } from "react-router-dom";

export default function ProfileModal() {
  const [showModal, setShowModal] = useState(false);

  return (
    <div className="relative">
      <button className="flex items-center" onClick={() => setShowModal(true)}>
        <img src="/images/profile.png" alt="profile" className="w-8 h-8" />
        <span className="text-xl text-[rgb(83,199,240)] ml-2 hidden sm:block">
          닉네임
        </span>
      </button>
      {showModal ? (
        <>
          <div className="absolute w-32 top-11 right-[-20px] outline-none focus:outline-none z-20">
            <div className="rounded-2xl shadow-lg relative flex flex-col w-full bg-white outline-none focus:outline-none">
              <div className="text-right"></div>
              <div className="mb-3">
                <Link to="/mypage">
                  <div className="mt-5 mb-1 text-base text-[rgb(83,199,240)] text-center">
                    마이페이지
                  </div>
                </Link>
                <Link to="/logout">
                  <div className="mt-5 mb-3 text-base text-[rgb(83,199,240)] text-center">
                    로그아웃
                  </div>
                </Link>
              </div>
            </div>
          </div>
          <button
            className="fixed inset-0"
            type="button"
            onClick={() => setShowModal(false)}
          ></button>
        </>
      ) : null}
    </div>
  );
}
