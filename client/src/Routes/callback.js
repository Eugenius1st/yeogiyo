import { useEffect } from "react";
import { useRecoilState } from "recoil";
import { useNavigate, useLocation } from "react-router-dom";
import {
  accessToken,
  refereshToken,
  userName,
  userAvatar,
  userId,
} from "../atoms/loginTest";
import axios from "axios";
import NotFoundW from "../components/notfound/NotFoundW";
import Header from "../components/Header";

export default function Callback() {
  const navigate = useNavigate();
  const location = useLocation();
  const tokenPath = location.search;
  const tokenInfo = tokenPath.split("%20");
  const accessTokenInfo = tokenInfo[1].split("&");

  const [, setAccessToken] = useRecoilState(accessToken);
  const [, setRefreshToken] = useRecoilState(refereshToken);
  const [, setUserName] = useRecoilState(userName);
  const [, setUserAvatar] = useRecoilState(userAvatar);
  const [, setUserId] = useRecoilState(userId);
  // console.log("tokenPath", tokenPath);
  // console.log("tokenInfo1", accessTokenInfo);
  useEffect(() => {
    const TOKEN = accessTokenInfo[0];
    const REFRESH_TOKEN = tokenInfo[2];
    setAccessToken(TOKEN);
    setRefreshToken(REFRESH_TOKEN);
    const config = {
      headers: { Authorization: `${TOKEN}` },
    };
    axios
      .get(
        "http://ec2-43-201-80-20.ap-northeast-2.compute.amazonaws.com:8080/members/header",
        config
      )
      .then((response) => {
        setUserName(response.data.name);
        setUserAvatar(response.data.avatar);
        setUserId(response.data.memberId);
      })
      .then(() => {
        navigate("/");
      })
      .catch(() => {
        alert("로그인 실패");
        navigate("initial");
      });
  }, []);

  return (
    <>
      <Header />
      <NotFoundW />
    </>
  );
}
