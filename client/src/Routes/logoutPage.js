import LoginHeader from "../components/LoginHeader";

// 로그인 테스트용입니다.
import { useRecoilState } from "recoil";
import { Link, useNavigate } from "react-router-dom";
import { userInfo, accessToken, refereshToken } from "../atoms/loginTest";

export default function LogoutPage() {
  const navigate = useNavigate();
  const [, setLoginState] = useRecoilState(userInfo);
  const [, setAccessToken] = useRecoilState(accessToken);
  const [, setRefreshToken] = useRecoilState(refereshToken);

  const userLogout = () => {
    // axios.post(`http://localhost:8080/logout`).then((response) => {
    alert("로그아웃 완료");

    setLoginState(""); // 일단 유저정보는 아바타만 들어있다
    setAccessToken("");
    setRefreshToken("");
    navigate("/initial");
    // });
  };

  return (
    <>
      <LoginHeader />
      <div className="lg:w-full w-full h-screen align-baseline flex justify-center items-center">
        {/*  */}
        <div className="max-w-md p-2 px-10 m-auto border border-[rgba(83,198,240,0.4)] rounded-xl text-[rgb(83,199,240)]">
          <div className="font-semibold border-b-2 border-[rgb(83,199,240)] w-fit px-5 py-2">
            Logout
          </div>
          <div className="relative flex justify-center items-center">
            <img
              src="images/gradation.png"
              alt="gradation"
              className="w-60 mx-7"
            />
            <img
              src="images/notfound_icon_w.png"
              alt="train"
              className="absolute w-16"
            />
          </div>
          <div className="font-semibold text-[rgb(83,199,240)] text-center my-10 ">
            로그아웃 하시겠습니까?
          </div>
          {/* <div className="text-white font-semibold m-auto w-fit  bg-gradient-to-tl from-white to-[rgb(83,199,240)] py-2 mb-4 px-6 rounded-md">
            Logout
          </div> */}

          {/* 로그인 테스트용입니다. */}
          <Link to="/">
            <div className="w-fit m-auto">
              <button
                className="text-white font-semibold w-fit  bg-gradient-to-tl from-white to-[rgb(83,199,240)] py-2 mb-4 px-6 rounded-md"
                onClick={() => userLogout(false)}
              >
                Logout
              </button>
            </div>
          </Link>
        </div>
      </div>
    </>
  );
}
